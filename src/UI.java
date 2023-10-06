import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

public class UI extends JFrame{

    public JTextField filename = new JTextField(), dir = new JTextField();

    private JTextField ipData = new JTextField();

    private JButton instructions = new JButton("Instructions");
    private JButton send = new JButton("Send File");
    private JButton receive = new JButton("Receive File");
    public JProgressBar progressBar = new JProgressBar(0,100);
    
    public DefaultListModel<String> lm = new DefaultListModel<>();  
    public JList<String> ipList = new JList<>(lm);

    int listenToThisPort = 8888;

    Container cp;
    JPanel panel;

    int width=0;
    int height=0;

    public UI(int width, int height){

        this.width=width;
        this.height=height;

        cp = getContentPane();
        panel = new JPanel();

        send.addActionListener(new SendL());
        panel.add(send);
        receive.addActionListener(new ReceiveL());
        panel.add(receive);
        instructions.addActionListener(new InstructionsL());
        panel.add(instructions);
        cp.add(panel, BorderLayout.SOUTH);
        
        panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        ipData.setEditable(false);ipData.setBackground(new Color(100, 255, 200, 255));ipData.setHorizontalAlignment(JTextField.CENTER);
        panel.add(ipData);
        dir.setEditable(false);dir.setHorizontalAlignment(JTextField.CENTER);
        panel.add(dir);
        filename.setEditable(false);filename.setHorizontalAlignment(JTextField.CENTER);
        panel.add(filename);

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 150, 0, 255));
        progressBar.setBackground(new Color(0, 0, 100, 255));
        
        
        BasicProgressBarUI ui = new BasicProgressBarUI() {
            protected Color getSelectionBackground() {
                return Color.red; // string color over the background
            }
            protected Color getSelectionForeground() {
                return Color.white; // string color over the foreground
            }
        };
        progressBar.setUI(ui);


        //progressBar.setSize(width/2, 100);
        progressBar.setVisible(true);
        panel.add(progressBar);
        cp.add(panel, BorderLayout.NORTH);

        panel = new JPanel();panel.setBackground(new Color(56, 78, 94, 255));

        ipList.setVisible(false);
        ipList.addListSelectionListener(new ListItemClick());
        panel.add(ipList);
        cp.add(panel, BorderLayout.CENTER);

        ipData.setText(Methods.getIpAddress() + " : " + listenToThisPort);
    }

    public void run(){
        Methods.findDevices();        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }
    
    String ipSelected = "";
    static boolean waiting = false;
    class ListItemClick implements ListSelectionListener{
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if(!waiting){
                ipSelected = ipList.getSelectedValue();
                System.out.println("ipSelected: " + ipSelected);
                waiting=true;
            }
        }
        
    }

    JFrame jFrame = null;
    class InstructionsL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(jFrame==null){
                jFrame = new JFrame();
                JEditorPane editorPane = new JEditorPane("text/html",Resources.instructions);
                editorPane.setEditable(false);
                editorPane.addHyperlinkListener(new HyperlinkListener() {
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            Desktop desktop = Desktop.getDesktop();
                            try {
                                desktop.browse(e.getURL().toURI());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                JPanel jPanel = new JPanel();
                jPanel.setBackground(new Color(56, 78, 94, 255));
                jPanel.add(editorPane);
                jFrame.getContentPane().add(jPanel, BorderLayout.CENTER);
                jFrame.setSize(width/2, height/2);
                jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                jFrame.setVisible(true);
            }else{
                jFrame.setVisible(true);
            }
        }
    }

    class ReceiveL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable(){
                @Override
                public void run(){
                    App.runSocketForFileReceiving();
                }
            }).start();
        }
    }

    class SendL implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            new Thread(new Runnable(){
                @Override
                public void run(){

                    ipList.setVisible(true);
                    waiting = false;
                    while("".equals(ipSelected)){
                        try {
                        Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        }
                    }
                    String tempIpSelected = ipSelected;
                    ipSelected="";
                    ipList.setVisible(false);

                    JFileChooser c = new JFileChooser();
                    c.setPreferredSize(new Dimension(800, 800));
                    c.setMultiSelectionEnabled(true);
                    
                    int rVal = c.showOpenDialog(UI.this);
                    if (rVal == JFileChooser.APPROVE_OPTION) {
                        dir.setText(c.getCurrentDirectory().toString());
                        System.out.println(c.getSelectedFile().getAbsolutePath());
                        App.runSocketForFileSending(c.getSelectedFiles(), tempIpSelected);
                    }
                    if (rVal == JFileChooser.CANCEL_OPTION) {
                        filename.setText("You pressed cancel");
                        dir.setText("");
                    }
                }
            }).start();
        }
    }

}
