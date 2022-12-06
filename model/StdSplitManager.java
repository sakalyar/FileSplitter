package serie10.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;

import serie02.util.FileStateTester;
import serie10.Splitter;
import util.Contract;

public class StdSplitManager extends Observable implements SplitManager {

    // ATTRIBUTS

    private File f;
    private FileStateTester fileState;
    private long[] splitsSizes;

    // CONSTRUCTEURS

    public StdSplitManager() {
        f = null;
        splitsSizes = null;
    }

    public StdSplitManager(File file) {
        Contract.checkCondition(file != null);

        f = file;
        if (canSplit()) {
            splitsSizes = new long[1];
            splitsSizes[0] = f.length();
        } else {
            splitsSizes = null;
        }
    }

    // REQUETES

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
        if (!canSplit()) {
            return 0;
        }
        return (int) Math.min(MAX_FRAGMENT_NB, Math.ceil((double)
                f.length() / (double) MIN_FRAGMENT_SIZE));
    }

    @Override
    public String[] getSplitsNames() {
        Contract.checkCondition(canSplit());

        String[] splitsNames = new String[splitsSizes.length];
        for (int i = 0; i < splitsSizes.length; i++) {
            splitsNames[i] = f.getAbsolutePath() + "." + (i + 1);
        }
        return splitsNames;
    }

    @Override
    public long[] getSplitsSizes() {
        Contract.checkCondition(canSplit(), "erreur fichier non concassable");

        long[] sizes = new long[splitsSizes.length];
        for (int i = 0; i < splitsSizes.length; i++) {
            sizes[i] = splitsSizes[i];
        }
        return sizes;
    }

    // COMMANDES

    @Override
    public void changeFor(File f) {
        Contract.checkCondition(f != null);
        this.f = f;
        if (canSplit()) {
            splitsSizes = new long[1];
            splitsSizes[0] = f.length();
        } else {
            splitsSizes = null;
        }
        setChanged();
        notifyObservers(Splitter.Modify.FILE_CHANGE);
    }

    @Override
    public void close() {
        f = null;
        splitsSizes = null;
    }

    @Override
    public void setSplitsSizes(long fragSize) {
        Contract.checkCondition(canSplit());
        Contract.checkCondition(fragSize >= Math.max(MIN_FRAGMENT_SIZE,
                Math.ceil((double) getFile().length() / (double)
                        getMaxFragmentNb())));

        splitsSizes = new long[(int) Math.ceil((double) getFile().length()
                / (double) fragSize)];
        for (int i = 0; i < splitsSizes.length; i++) {
            splitsSizes[i] = fragSize;
        }
        if ((getFile().length() % fragSize) != 0) {
            splitsSizes[splitsSizes.length - 1] = getFile().length() % fragSize;
        }
        setChanged();
        notifyObservers(Splitter.Modify.CONFIG_CHANGE);
    }

    @Override
    public void setSplitsSizes(long[] fragSizes) {
        Contract.checkCondition(canSplit());
        Contract.checkCondition(fragSizes != null);
        Contract.checkCondition(fragSizes.length >= 1);

        long sumFragLength = 0;
        for (long i: fragSizes) {
            Contract.checkCondition(i >= MIN_FRAGMENT_SIZE);

            sumFragLength += i;
        }
        if (sumFragLength < getFile().length()) {
            Contract.checkCondition(fragSizes.length < getMaxFragmentNb());

            splitsSizes = new long[fragSizes.length + 1];
            for (int i = 0; i < fragSizes.length; i++) {
                splitsSizes[i] = fragSizes[i];
            }
            splitsSizes[fragSizes.length] = getFile().length() - sumFragLength;
        } else {
            Contract.checkCondition(fragSizes.length <= getMaxFragmentNb());

            splitsSizes = new long[fragSizes.length];
            for (int i = 0; i < fragSizes.length - 1; i++) {
                splitsSizes[i] = fragSizes[i];
            }
            splitsSizes[fragSizes.length - 1] = getFile().length()
                    - (sumFragLength - fragSizes[fragSizes.length - 1]);
        }
    }

    @Override
    public void setSplitsNumber(int number) {
        Contract.checkCondition(canSplit());
        Contract.checkCondition(1 <= number && number <= getMaxFragmentNb());

        splitsSizes = new long[number];
        if ((getFile().length() / number) < MIN_FRAGMENT_SIZE) {
            for (int i = 0; i < number - 1; i++) {
                splitsSizes[i] = MIN_FRAGMENT_SIZE;
            }
            splitsSizes[number - 1] = getFile().length() - (number - 1)
                    * MIN_FRAGMENT_SIZE;
        } else {
            for (int i = 0; i < getFile().length() % number; i++) {
                splitsSizes[i] = getFile().length() / number + 1;
            }
            for (int i = (int) getFile().length() % number; i < number; i++) {
                splitsSizes[i] = getFile().length() / number;
            }
        }
        setChanged();
        notifyObservers(Splitter.Modify.CONFIG_CHANGE);
    }

    @Override
    public void split() throws IOException {
        Contract.checkCondition(canSplit());

        BufferedInputStream input = new BufferedInputStream(new FileInputStream(
                        f.getAbsolutePath()));
        int c = input.read();
        for (int i = 0; i < getSplitsNames().length; i++) {
            BufferedOutputStream output = new BufferedOutputStream(
                            new FileOutputStream(getSplitsNames()[i]));
            long k = 0;
            while (c != -1 && k < splitsSizes[i]) {
                output.write(c);
                k++;
                c = input.read();
            }
            output.close();
        }
        input.close();
        setChanged();
        notifyObservers();
    }
}