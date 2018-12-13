/*  [ClientGUI.java]
    JFrame handling messaging for the program and allowing user interaction
    Author: Brian Zhang
    ICS4UE
    Date: 12/04/18
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class ClientGUI extends JFrame implements ActionListener {

    //Text Label
    private JLabel instructions;

    //Chat TextField SetUp
    private JTextField enterField;
    private JTextField serverField, portField;
    private JTextArea hub;

    //Button SetUp
    private JButton login, logout, whoIsIn;

    //Connection Check
    private boolean connected;

    //Client object
    private Client client;

    //Receives socket
    ClientGUI() {

        super("harmony");

        //Custom Labels
        Image harmonyLogo = new ImageIcon("harmonynote.png").getImage();
        setIconImage(harmonyLogo);

        try {
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("renogare.otf")));
        } catch (IOException | FontFormatException e) {
            //Handle exception
        }

        //Custom UI Font
        setUIFont(new javax.swing.plaf.FontUIResource("renogare", Font.PLAIN, 12));


        //GridLayout Panels
        JPanel northPanel = new JPanel(new GridLayout(3, 1));
        // the server name and the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));
        // the two JTextField with default value for server address and port number
        serverField = new JTextField();
        serverField.setBackground(new Color(254, 234, 255));
        portField = new JTextField();
        portField.setBackground(new Color(254, 234, 255));
        portField.setHorizontalAlignment(SwingConstants.RIGHT);

        serverAndPort.add(new JLabel("|server address|:  "));
        serverAndPort.add(serverField);
        serverAndPort.add(new JLabel("|port number|:  "));
        serverAndPort.add(portField);
        serverAndPort.add(new JLabel(""));
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);

        // The CenterPanel which is the chat room
        hub = new JTextArea("|write something meaningful today.|\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));

        // the Label and the TextField
        instructions = new JLabel("|enter your username below|", SwingConstants.CENTER);
        northPanel.add(instructions);
        enterField = new JTextField("anon");
        enterField.setBackground(new Color(255, 231, 234));
        northPanel.add(enterField);
        add(northPanel, BorderLayout.NORTH);


        JScrollPane sc = new JScrollPane(hub);
        centerPanel.add(sc);

        hub.setEditable(true);
        hub.setBackground(new Color(255, 231, 234));
        add(centerPanel, BorderLayout.CENTER);

        // the 3 buttons
        login = new JButton("|login|");
        logout = new JButton("|logout|");
        logout.setEnabled(false);
        whoIsIn = new JButton("|who is in|");
        whoIsIn.setEnabled(false);

        //Add Action Listener
        login.addActionListener(this);
        logout.addActionListener(this);
        whoIsIn.addActionListener(this);


        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setVisible(true);
        enterField.requestFocus();

    }

    // called by the Client to append text in the TextArea
    public void append(String str) {
        hub.append(str);
        hub.setCaretPosition(hub.getText().length() - 1);
    }

    // called by the GUI is the connection failed
    // we reset our buttons, label, textfield
    public void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        instructions.setText("|enter your username below|");
        enterField.setText("anon");
        // reset port number and host name as a construction time
        portField.setText("");
        serverField.setText("");
        // let the user change them
        serverField.setEditable(false);
        portField.setEditable(false);
        // don't react to a <CR> after the username
        enterField.removeActionListener(this);
        connected = false;
    }

    /*
     * Button or JTextField clicked
     */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it is the Logout button
        if(o == logout) {
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            return;
        }
        // if it the who is in button
        if(o == whoIsIn) {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            return;
        }

        // ok it is coming from the JTextField
        if(connected) {
            // just have to send the message
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, enterField.getText()));
            enterField.setText("");
            return;
        }


        if(o == login) {
            // ok it is a connection request
            String username = enterField.getText().trim();
            // empty username ignore it
            if(username.length() == 0)
                return;
            // empty serverAddress ignore it
            String server = serverField.getText().trim();
            if(server.length() == 0)
                return;
            // empty or invalid port numer, ignore it
            String portNumber = portField.getText().trim();
            if(portNumber.length() == 0)
                return;
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                return;   // nothing I can do if port number is not valid
            }

            // try creating a new Client with GUI
            client = new Client(server, port, username, this);
            // test if we can start the Client
            if(!client.start())
                return;
            enterField.setText("");
            instructions.setText("enter your message below");
            connected = true;

            // disable login button
            login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);
            // disable the Server and Port JTextField
            serverField.setEditable(false);
            portField.setEditable(false);
            // Action listener for when the user enter a message
            enterField.addActionListener(this);
        }

    }

    //Try Custom Font
    public static void setUIFont (javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }
}
