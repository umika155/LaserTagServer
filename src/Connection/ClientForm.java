package Connection;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class ClientForm extends JFrame{

    String serverIP, gunName, softwareVersion, deviceType;
    
    int serverPort = 2222;
    Boolean isConnected = false;
    
    Socket socket;
    BufferedReader netin;
    PrintWriter netout;

    Timer timer;
    boolean stopFlag = true;
    int globalCount = 0;
    
    public ClientForm() {
        initComponents();
        
        timer = new Timer("Timer");
        
        long delay  = 1000L;
        long period = 1000L;

        timer.scheduleAtFixedRate(repeatedTask, delay, period);
    }

    TimerTask repeatedTask = new TimerTask() {
        public void run() {
        	if (!stopFlag) {
        		globalCount++;

        		JSONArray arrJSON = new JSONArray();
				arrJSON.add("message");
				arrJSON.add(String.valueOf(globalCount));

				netout.println(arrJSON.toJSONString());
        		netout.flush();
        	}
        }
    };

    public void ListenThread() 
    {
         Thread IncomingReader = new Thread(new IncomingReader());
         IncomingReader.start();
    }
    
    public void sendDisconnect() 
    {
        try
        {
        	JSONArray arrJSON = new JSONArray();
			arrJSON.add("disconnection");

			netout.println(arrJSON.toJSONString());
            netout.flush(); 
        } catch (Exception e) 
        {
            txtAreaOutput.append("Could not send disconnect message.\n");
        }
    }
    
    public void Disconnect() 
    {
        try 
        {
            txtAreaOutput.append("Disconnected.\n");
            socket.close();
        } catch(Exception ex) {
            txtAreaOutput.append("Failed to disconnect. \n");
        }
        isConnected = false;
        
        txtIPAddress.setEditable(true);
        txtDeviceType.setEditable(true);
        txtGunName.setEditable(true);
        txtSoftwareVersion.setEditable(true);
        
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnSendMsg.setEnabled(false);
    }
    
    
    public class IncomingReader implements Runnable
    {
        @Override
        public void run() 
        {
            String message;
            
            try 
            {
                while ((message = netin.readLine()) != null) 
                {
                    
                	JSONParser parser = new JSONParser();
        	        JSONArray arrJSON = (JSONArray) parser.parse(message);

                	if(arrJSON.size() != 0) {
    					txtAreaOutput.append("Received from server:\n");
                		for(int i = 0; i < arrJSON.size(); i++)
                			if(!arrJSON.get(i).equals("")) txtAreaOutput.append(arrJSON.get(i) + "\n");
                	}

                }
           } catch(Exception ex) { }
        
        }
    }

    private void initComponents() {

        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        lblMainTitle = new javax.swing.JLabel();
        btnConnect = new javax.swing.JButton();
        btnDisconnect = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaOutput = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        txtMsg = new javax.swing.JTextField();
        btnSendMsg = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtServerIPAdd = new javax.swing.JTextPane();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblIPAddress = new javax.swing.JLabel();
        lblGunName = new javax.swing.JLabel();
        lblSoftwareVer = new javax.swing.JLabel();
        lblDeviceType = new javax.swing.JLabel();
        txtDeviceType = new javax.swing.JTextField();
        txtSoftwareVersion = new javax.swing.JTextField();
        txtGunName = new javax.swing.JTextField();
        txtIPAddress = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        jFormattedTextField1.setText("jFormattedTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        //lblMainTitle.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        lblMainTitle.setFont(new java.awt.Font("Sitka Small", 1, 28)); // NOI18N
        lblMainTitle.setText("Laser Tag Device Simulator");

        btnConnect.setText("Connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        btnDisconnect.setText("Disconnect");
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });

        txtAreaOutput.setColumns(20);
        txtAreaOutput.setRows(5);
        txtAreaOutput.setEditable(false);
        jScrollPane1.setViewportView(txtAreaOutput);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Communication Log:");

        btnSendMsg.setText("Send Message");
        btnSendMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendMsgActionPerformed(evt);
            }
        });

        // Find public IP address 
        String systemipaddress = ""; 
        
        InetAddress localhost;
		try {
			localhost = InetAddress.getLocalHost();
	        systemipaddress = localhost.getHostAddress().trim();
	        
	        txtIPAddress.setText(systemipaddress);
	        txtServerIPAdd.setText("MyIP Address:" + systemipaddress);
	        
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			txtServerIPAdd.setText("MyIP Address: NONE");
			e1.printStackTrace();
		}
		
        txtServerIPAdd.setEditable(false);
        jScrollPane2.setViewportView(txtServerIPAdd);

        btnStart.setText("START");
        btnStart.setEnabled(false);
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	btnStartActionPerformed(evt);
            }
        });
        
        btnStop.setText("STOP");
        btnStop.setEnabled(false);
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	btnStopActionPerformed(evt);
            }
        });
        
        lblIPAddress.setText("Server IP Address:");

        lblGunName.setText("Gun Name:");
        txtGunName.setText("GUN");
        
        lblSoftwareVer.setText("Software Version:");
        txtSoftwareVersion.setText("1.0");
        
        lblDeviceType.setText("Device Type:");
        txtDeviceType.setText("TYPE");
        
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGunName, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoftwareVer, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeviceType, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDeviceType, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSoftwareVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtGunName, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(lblIPAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtGunName)
                    .addComponent(lblGunName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSoftwareVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoftwareVer, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDeviceType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeviceType, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Connection Detail:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addComponent(btnConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(btnDisconnect, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnSendMsg))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(161, 161, 161)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(lblMainTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnStart)
                                .addGap(30, 30, 30)
                                .addComponent(btnStop))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMainTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnStart)
                        .addComponent(btnStop)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMsg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSendMsg)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnConnect)
                            .addComponent(btnDisconnect))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }
    
    private void btnSendMsgActionPerformed(java.awt.event.ActionEvent evt) {
        
        String nothing = "";
        if ((txtMsg.getText()).equals(nothing)) {
            txtMsg.setText("");
            txtMsg.requestFocus();
        } else {
            try {
                
				JSONArray arrJSON = new JSONArray();
				arrJSON.add("message");
				arrJSON.add(txtMsg.getText());
    	         
				netout.println(arrJSON.toJSONString());
				netout.flush();
            } catch (Exception ex) {
                txtAreaOutput.append("Message was not sent. \n");
            }
            txtMsg.setText("");
            txtMsg.requestFocus();
        }

        txtMsg.setText("");
        txtMsg.requestFocus();
    }

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {
        
    	stopFlag = false;
    	
    }

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {
        
    	stopFlag = true;
    	
    }
    
    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        if (isConnected == false) 
        {
        	serverIP = txtIPAddress.getText();
            gunName = txtGunName.getText();
            softwareVersion = txtSoftwareVersion.getText();
            deviceType = txtDeviceType.getText();
            
            txtIPAddress.setEditable(false);
            txtDeviceType.setEditable(false);
            txtGunName.setEditable(false);
            txtSoftwareVersion.setEditable(false);

            try 
            {
            	socket = new Socket(serverIP, serverPort);
                InputStreamReader streamreader = new InputStreamReader(socket.getInputStream());
                netin = new BufferedReader(streamreader);
                netout = new PrintWriter(socket.getOutputStream());
                
                JSONArray arrJSON = new JSONArray();
				arrJSON.add("connection");
				arrJSON.add(gunName);
				arrJSON.add(softwareVersion);
				arrJSON.add(deviceType);
				
				netout.println(arrJSON.toJSONString());
        		netout.flush(); 
                isConnected = true;
                
                btnConnect.setEnabled(false);
                btnDisconnect.setEnabled(true);
                btnSendMsg.setEnabled(true);
                btnStart.setEnabled(true);
                btnStop.setEnabled(true);
            } 
            catch (Exception ex) 
            {
                txtAreaOutput.append("Cannot Connect! Try Again. \n");
                txtIPAddress.setEditable(true);
                txtDeviceType.setEditable(true);
                txtGunName.setEditable(true);
                txtSoftwareVersion.setEditable(true);
                
                btnConnect.setEnabled(true);
                btnDisconnect.setEnabled(false);
                btnSendMsg.setEnabled(false);
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
            }
            
            ListenThread();
            
        } else if (isConnected == true) 
        {
            txtAreaOutput.append("You are already connected. \n");
        }
    }

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {
        sendDisconnect();
        Disconnect();
    }

    public static void main(String args[]) {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnSendMsg;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDeviceType;
    private javax.swing.JLabel lblGunName;
    private javax.swing.JLabel lblIPAddress;
    private javax.swing.JLabel lblMainTitle;
    private javax.swing.JLabel lblSoftwareVer;
    private static javax.swing.JTextArea txtAreaOutput;
    private javax.swing.JTextField txtDeviceType;
    private javax.swing.JTextField txtGunName;
    private javax.swing.JTextField txtIPAddress;
    private javax.swing.JTextField txtMsg;
    private static javax.swing.JTextPane txtServerIPAdd;
    private javax.swing.JTextField txtSoftwareVersion;
    // End of variables declaration//GEN-END:variables
}
