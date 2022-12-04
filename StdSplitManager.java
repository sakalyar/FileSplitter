package serie10;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Observable;
import java.util.Scanner;

import serie02.util.FileStateTester;
import util.Contract;

public class StdSplitManager extends Observable implements SplitManager {

	private File f;
	
	private long splitsSizes[];
	private long gSS;
	private long lg;
	
	private long sizesArray[];
	private Integer CONFIG_CHANGE = 0;
	private String FILE_CHANGE = "";
	public StdSplitManager() {
		f = null;
	}
	public StdSplitManager(File file) {
		Contract.checkCondition(file != null, 
				"Le fichier ne doit pas être null.");
		changeFor(file);
		setChanged();
        notifyObservers();
	}

	@Override
	public boolean canSplit() {
		return FileStateTester.isSplittable(f);
	}

	@Override
	public String getDescription() {
		return FileStateTester.describe(f);
	}

	@Override
	public File getFile() {
		return f;
	}

	@Override
	public int getMaxFragmentNb() {
		if (getFile() == null) return 0;
		int m = (int) Math.max(
				1, Math.ceil((double) getFile().length() / MIN_FRAGMENT_SIZE));
		return Math.min(MAX_FRAGMENT_NB, m);
	}

	@Override
	public String[] getSplitsNames() {
		int size = getSplitsSizes().length;
		String[] output = new String[size];
    	for (int i = 0; i < size; ++i)
    		output[i] = getFile().getAbsolutePath() + "." + (i + 1);
    	return output;
	}

	@Override
	public long[] getSplitsSizes() {
		if (getFile() == null) return null;
		long[] output = new long[splitsSizes.length];
    	for (int i = 0; i < splitsSizes.length; i++) {
    		output[i] = splitsSizes[i];
    	}
    	return output;
	}

	@Override
	public void changeFor(File f) {
		this.f = f;
	    splitsSizes = new long[1];
		splitsSizes[0] = f.length();
		setChanged();
        notifyObservers(FILE_CHANGE);
	}

	@Override
	public void close() {
		f = null;
	}

	@Override
	public void setSplitsSizes(long size) {
		if (getFile() != null)
			if (canSplit() && size >= Math.ceil(getFile().length() / getMaxFragmentNb())) {
				gSS = (long) Math.ceil(getFile().length() / size);
			}
		setChanged();
        notifyObservers(CONFIG_CHANGE);
	}

	@Override
	public void setSplitsSizes(long[] sizes) {
		int lengthOfSizes = sizes.length;
		long sumOfPassedLengths = Arrays.stream(sizes).sum();
		lg = getFile().length();
		if (canSplit() && lengthOfSizes > 0 &&
				((sumOfPassedLengths - sizes[lengthOfSizes-1] < lg && lengthOfSizes < getMaxFragmentNb()) ||
				(sumOfPassedLengths - sizes[lengthOfSizes-1] >= lg && lengthOfSizes <= getMaxFragmentNb()
				))
			) 
		{
			if (lg > sumOfPassedLengths) {
				sizesArray = new long[lengthOfSizes+1];
				sizesArray[lengthOfSizes] = lg - sumOfPassedLengths;
			} else {
				sizesArray = new long[lengthOfSizes];
				sizesArray[lengthOfSizes-1] = (sumOfPassedLengths == lg) ? sizes[lengthOfSizes-1] : sumOfPassedLengths - lg;
			}
			for (int i = 0; i < sizes.length-1; ++i)
				sizesArray[i] = sizes[i];
		}
		setChanged();
        notifyObservers();
	}

	@Override
	public void setSplitsNumber(int splitsNb) {
		Contract.checkCondition(getFile().length() > 0,
    			"Le fichier doit être de taille supérieure de 0.");
    	Contract.checkCondition(1 < splitsNb && splitsNb <= getMaxFragmentNb() || splitsNb == 1,
    			"La condition doit être vérifée : " 
    			+ "1 <= number <= getMaxFragmentNb()");
    	long n = getFile().length();
    	long s = n / splitsNb;
    	if (s < MIN_FRAGMENT_SIZE) {
    		int size = (int) Math.ceil((double) n / MIN_FRAGMENT_SIZE);
    		splitsSizes = new long[size];
    		long sum = 0;
    		for (int i = 0; i < size - 1; i++) {
    			splitsSizes[i] = MIN_FRAGMENT_SIZE;
    			sum += MIN_FRAGMENT_SIZE;
    		}
    		splitsSizes[size - 1] = n - sum;
    		return;
    	}
    	splitsSizes = new long[splitsNb];
    	int k = (int) n % splitsSizes.length;
    	for (int i = 0; i < k; i++)
    		splitsSizes[i] = s + 1;
    	for (;k < splitsSizes.length;)
    		splitsSizes[k++] = s;		
    	setChanged();
        notifyObservers();
	}
	
	
	@Override
	public void split() throws IOException {
		InputStream in = null;
		int n = getMaxFragmentNb();
		long r = f.length() % n;
		long k = f.length() / n;
		if (r > 0)
			k++;
		try {
			in = new BufferedInputStream(new FileInputStream(f));
			for (int i = 0; i < k; i++) {
				String name = f.getAbsolutePath() + "." + (i+1);
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(name));
				long lg = i == k - 1 && r > 0 ? r: n;
				for (int j = 0; j < lg; j++) {
					out.write(in.read());
				}
				out.close();
			}
		} catch (IOException e) {
			System.out.println("CRITICAL ERROR");
		}
		setChanged();
	    notifyObservers();
	}
}
