package serie10;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Splitter {
	private static final Integer CONFIG_CHANGE = 0;
	private static final String FILE_CHANGE = "";
	
	private JFileChooser fileChooser;
	private JFrame returnFrame;
	
	private JTextField splitFileName;
	private JButton browse;
	
	private JComboBox<Integer> splitFragmentsNb;
	DefaultComboBoxModel<Integer> comboBoxModel = new DefaultComboBoxModel<Integer>();
	
	private JTextField splitFragmentsSize;
	private JButton splitFile;
	private JTextArea description;
	private JFrame frame;
	private final String initialDescription = 
			"Taille total du fichier: non défini\n\n"
			+ "Description des fragments: non défini\n\n"
			+ "Taille moyenne d\'un fragement: non défini"; 
	private String FILE_NAME;
	private int fragmentsNb;
	private SplitManager model;
	
	public Splitter() {
		createModel();
		createView();
		placeComponents();
		createController();
	}
	
	private void display() {
		refresh();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void createModel() {
		model = new StdSplitManager();
		
	}
	
	private void createView() {
		frame = new JFrame();
		
		splitFileName = new JTextField(16);
		browse = new JButton ("Parcourir");
		splitFragmentsSize = new JTextField();
		
		comboBoxModel.addElement(55);
		comboBoxModel.addElement(155);
		splitFragmentsNb = new JComboBox<Integer>(comboBoxModel);
		splitFragmentsSize.setPreferredSize(new Dimension(80, 20));
		splitFile = new JButton ("Fragmenter!");
		splitFile.setEnabled(false);
		description = new JTextArea(initialDescription);
		description.setEditable(false);
		description.setDisabledTextColor(Color.GRAY);
		
		// Pour ouvrir la fenetre du chargement du fichier
		returnFrame = new JFrame();
		
	}
	
	
	private void placeComponents() {
		
		//Création d'un main frame 
		JPanel p = new JPanel();
		{ //--
			JLabel fileLabel = new JLabel("Fichier à fragmenter");
			p.add(fileLabel);
			p.add(splitFileName);
			p.add(browse);
		} //--
		frame.add(p, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		{ //--
			scrollPane.setViewportView(description);
		} //--
		frame.add(scrollPane, BorderLayout.CENTER);
		
		p = new JPanel(new GridLayout(2, 1));
		{ //--
			JPanel q = new JPanel(new BorderLayout());
			{ //--
				JPanel r = new JPanel(new GridLayout(2, 2));
				{
					JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					{ //--
						JLabel fragmentsLabel = new JLabel("Nb. fragments");
						s.add(fragmentsLabel);
					} //--
					r.add(s);
					s = new JPanel(new FlowLayout(FlowLayout.LEFT));
					{ //--
						s.add(splitFragmentsNb);
					} //--
					r.add(s);
					s = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					{ //--
						JLabel size = new JLabel("Taille des fragments"); 
						s.add(size);
					} //--
					r.add(s);
					s = new JPanel(new FlowLayout(FlowLayout.LEFT));
					{ //--
						s.add(splitFragmentsSize);
						JLabel octetsLabel = new JLabel("octets");
						s.add(octetsLabel, BorderLayout.WEST);
					} //--
					r.add(s);
				}
				q.add(r, BorderLayout.SOUTH);
			} //--
			p.add(q);
			q = new JPanel(new FlowLayout(FlowLayout.CENTER));
			{ //--
				q.add(splitFile, BorderLayout.NORTH);
			} //--
			p.add(q);
		} //--
		frame.add(p, BorderLayout.WEST);
		
		JLabel AddtitionalInformation = new JLabel("(*) Il s\'agit de la taille de chaque fragment à un octet près," + 
													"sauf peut-etre le dernier fragment");
		frame.add(AddtitionalInformation, BorderLayout.SOUTH);
		
		// Création d'une fenetre d'ouverture:
		p = new JPanel();
		{ //--
			LayoutManager layout = new FlowLayout();
			p.setLayout(layout);
		} //--
		returnFrame.add(p);
	}
	private void createController() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fileChooser = new JFileChooser();
		
        ((Observable) model).addObserver(new Observer() {
        	
            @Override
            public void update(Observable o, Object arg) {
				refresh();

            	if (arg == CONFIG_CHANGE) {
            		
            		splitFragmentsSize.setText(Long.toString(model.getSplitsSizes()[0]));
            		
            	} else if (arg == FILE_CHANGE) {
            		System.out.println("Changed file");
            		if ((String) arg == FILE_CHANGE) {
            			
            			if (model.canSplit()) {
            				System.out.println("can split");
            			} else {
            				
            			}
            		};
            	}
            }
        });
        
        browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				int result = fileChooser.showOpenDialog(returnFrame);
        		if (result == JFileChooser.APPROVE_OPTION) {
        			File f = fileChooser.getSelectedFile();
					System.out.println("Opening: " + f.getName() + ".\n");
					model.changeFor(f);
					
					DefaultComboBoxModel<Integer> newComboModel = new DefaultComboBoxModel<Integer>();
					for (int i = 0, maxNb = model.getMaxFragmentNb(); i < maxNb; ++i)
						newComboModel.addElement(i+1);
					comboBoxModel = newComboModel;
					splitFragmentsNb.setModel(comboBoxModel);
					System.out.println(splitFragmentsNb.getItemAt(2));
					comboBoxModel.setSelectedItem(model.getMaxFragmentNb());
					
					
					model.changeFor(f);
        		}
			}
        });
        
        splitFragmentsNb.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		if (splitFragmentsNb.getSelectedItem() == null) return;
    			fragmentsNb = (int)splitFragmentsNb.getSelectedItem();
        		int size = splitFragmentsNb.getItemCount();
        		for (int i = 0; i < size; i++) {
        		  int item = splitFragmentsNb.getItemAt(i);
        		  System.out.println("Item at " + i + " = " + item);
        		}
        		
        	}
        });
        
        splitFile.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
//				int result = fileChooser.showOpenDialog(returnFrame);
//        		if (result == JFileChooser.APPROVE_OPTION) {
//					File f = fileChooser.getSelectedFile();
//					System.out.println("Opening: " + f.getName() + ".\n");
//					model.changeFor(f);
//					model.setSplitsNumber(3);
//					try {
//						model.split();
//						double averageFragSize = (double) (model.getFile().length() / model.getSplitsSizes().length);
//						} catch(IOException ex) {
//							System.out.println("An error has occured");
//						}
//				} else {
//					System.out.println("Out of luck!");
//				}
        	}
        });
        
	}
	
	public void refresh() {
		
		Container contentPane = frame.getContentPane();
		
		splitFragmentsSize.setText(model.getSplitsSizes() == null ? "" : String.valueOf(model.getSplitsSizes()[0]));
		if (model.canSplit()) {
			description.setText(model.getDescription());			
		}
//		splitFragmentsNb.setSelectedIndex(model.getMaxFragmentNb()-1);
	}
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Splitter().display();
            }
        });
    }
	
}
