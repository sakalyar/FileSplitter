package serie10;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;


import serie10.model.SplitManager;
import serie10.model.StdSplitManager;

public class Splitter {

    public enum Modify {
        CONFIG_CHANGE(),
        FILE_CHANGE();
        Modify() {}
    };

    // ATTRIBUTS
    private SplitManager model;
    private JFrame mainFrame;
    private JTextField splitFileName;
    private JButton browse;
    private JComboBox<Integer> splitFragmentNb;
    private JTextField splitFragmentSize;
    private JButton split;
    private JTextArea splitDescription;


    // CONSTRUCTEURS
    public Splitter() {
        createModel();
        createView();
        placeComponents();
        createController();
    }

    // COMMANDES
    /**
     * Rend l'application visible au centre de l'écran.
     */
    public void display() {
        refresh(null);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    // OUTILS
    private void createModel() {
        model = new StdSplitManager();
    }

    private void createView() {
        mainFrame = new JFrame("Fragmenteur de fichiers");

        splitFileName = new JTextField();
        final int columns = 20;
        splitFileName.setColumns(columns);

        browse = new JButton("Parcourir...");

        splitFragmentNb = new JComboBox<Integer>();

        splitFragmentSize = new JTextField();
        splitFragmentSize.setColumns(6);

        split = new JButton("Fragmenter!");

        splitDescription = new JTextArea();
    }

    private void placeComponents() {
        JPanel p = new JPanel(); {
            p.setLayout(new FlowLayout(FlowLayout.CENTER));
            p.add(new JLabel("Fichier à fragmenter:"));
            p.add(splitFileName);
            p.add(browse);
        }
        mainFrame.add(p, BorderLayout.NORTH);

        p = new JPanel(); 
        { //--
            p.setLayout(new GridLayout(2, 1));
            JPanel q = new JPanel(new BorderLayout()); 
            { //--
                { //--
                    JPanel r = new JPanel(new GridLayout(2, 2)); 
                    { //--
                        JPanel s = new JPanel(); 
                        { //--
                            s.setLayout(new FlowLayout(FlowLayout.RIGHT));
                            s.add(new JLabel("Nb. fragments:"));
                        } //--
                        r.add(s);
                        s = new JPanel(); 
                        { //--
                            s.setLayout(new FlowLayout(FlowLayout.LEFT));
                            s.add(splitFragmentNb);
                        } //--
                        r.add(s);
                        s = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
                        { //--
                            s.add(new JLabel("Taille des fragments*:"));
                        } //--
                        r.add(s);
                        s = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
                        { //--
                            s.add(splitFragmentSize);
                            s.add(new JLabel("octets"));
                        } //--
                        r.add(s);
                    } //--
                    q.add(r, BorderLayout.SOUTH);
                } //--
            } //--
            p.add(q);
            q = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
            { //--sdsd
                q.add(split);
            } //--
            p.add(q);
        } //--
        mainFrame.add(p, BorderLayout.WEST);

        mainFrame.add(new JScrollPane(splitDescription), BorderLayout.CENTER);

        mainFrame.add(new JLabel("(*) Il s'agit de la taille de chaque fragment"
                + " à un octet près, sauf peut-être pour le dernier fragment."),
                BorderLayout.SOUTH);
    }

    private void createController() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        	((Observable) model).addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
            	refresh(arg);
            }
        });

        splitFileName.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                model.changeFor(new File(splitFileName.getText()));
            }
        });

        browse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                    model.changeFor(chooser.getSelectedFile());
                }
            }
        });

        splitFragmentNb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setSplitsNumber(splitFragmentNb.getSelectedIndex() + 1);
            }
        });

        splitFragmentSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    long fragSize = Long.valueOf(splitFragmentSize.getText());
                    if (fragSize < SplitManager.MIN_FRAGMENT_SIZE) {
                        throw new NumberFormatException();
                    }
                    model.setSplitsSizes(fragSize);
                } catch (NumberFormatException exception) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Impossible de splitter: valeur anormale",
                        "Erreur !",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        split.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    model.split();
                    JOptionPane.showMessageDialog(
                        null,
                        "Split réussi !",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Split raté",
                        "Erreur !",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }

            }
        });
    }

    private void refresh(Object arg) {
        if (arg == Modify.FILE_CHANGE || arg == null) {
            splitFragmentNb.setEnabled(model.canSplit());
            splitFragmentSize.setEnabled(model.canSplit());
            split.setEnabled(model.canSplit());
            splitDescription.setEnabled(model.canSplit());

            MutableComboBoxModel<Integer> combo = new DefaultComboBoxModel<Integer>();
            for (int i = 1; i <= model.getMaxFragmentNb(); i++) {
                combo.addElement(i);
            }
            splitFragmentNb.setModel(combo);

            if (!model.canSplit()) {
                splitFragmentSize.setText("");

                splitDescription.setText("Taille totale du fichier : non défini\n\n");
                splitDescription.append("Description des fragments du fichier :\n");
                splitDescription.append(" non défini\n\n");
                splitDescription.append("Taille moyenne d'un fragment : non défini.");
            }
        }

        if (arg == Modify.FILE_CHANGE) {
            splitFileName.setText(model.getFile().getAbsolutePath());

            if (!model.canSplit()) {
                splitFileName.setText("Ceci n'est pas le nom d'un fichier "
                        + "valide");

                JOptionPane.showMessageDialog(
                    null,
                    "Ceci n'est pas le nom d'un fichier valide",
                    "Erreur !",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }

        if (arg == Modify.CONFIG_CHANGE || model.canSplit()) {
            splitFragmentSize.setText(String.valueOf(
                    model.getSplitsSizes()[0]));

            splitDescription.setText("Taille totale du fichier : ");
            splitDescription.append(String.valueOf(
                    model.getFile().length()));
            splitDescription.append(" octets\n\n");
            splitDescription.append("Description des fragments du fichier :\n");
            for (int i = 0; i < model.getSplitsSizes().length; i++) {
                splitDescription.append(" " + model.getSplitsNames()[i]
                        + " : " + model.getSplitsSizes()[i] + " octets\n");
            }
            splitDescription.append("\n");
            splitDescription.append("Taille moyenne d'un fragment : ");
            splitDescription.append(String.valueOf(
                 model.getFile().length() / model.getSplitsSizes().length));
            splitDescription.append(" octets.");

            ActionListener[] tab = splitFragmentNb.getActionListeners();
            for (ActionListener a:tab) {
                splitFragmentNb.removeActionListener(a);
            }
            splitFragmentNb.setSelectedIndex(model.getSplitsSizes().length - 1);
            for (ActionListener a:tab) {
                splitFragmentNb.addActionListener(a);
            }
        }
    }

    // POINT D'ENTREE
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Splitter().display();
            }
        });
    }
}
