package ConnectionWithoutJSONDB;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.sql.*;
import java.text.SimpleDateFormat;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class Server extends javax.swing.JFrame {
    
    Vector<Socket> clientSockets = new Vector<Socket>();
    DefaultTableModel tableModel;
    Statement dbStatement;
    
    Vector<Thread> threads = new Vector<Thread>();
    Vector<ClientHandler> clientHandlers = new Vector<ClientHandler>();
    
    public Server() {
    	
    	initComponents();
        try {
        	
        	/*Class.forName("com.mysql.jdbc.Driver");  
    		Connection con=DriverManager.getConnection("jdbc:mysql:///lasertagdb","root","");  
    		dbStatement=con.createStatement();*/
    		
        	Thread starter = new Thread(new ServerStart());
            starter.start();
        
            btnSendtoSelect.setEnabled(true);
            btnSendtoAll.setEnabled(true);
        
            txtAreaOutputAppendAtLast("Server started...");
            
        } catch (Exception e) {
        	
            System.out.println("Error: " + e);
            
        }
    }
    
    public class ServerStart implements Runnable 
    {
        public void run() 
        {
            try 
            {
                @SuppressWarnings("resource")
				ServerSocket serverSock = new ServerSocket(2222);
                
                //running infinite loop for getting client request
                while (true) 
                {
                    //Accept a new connection
                    Socket clientSocket = serverSock.accept();
                    //clientSocket.setSoTimeout(3000);

                    //Create a new thread object
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.add(clientHandler);
                    
                    Thread listener = new Thread(clientHandler);
                    threads.add(listener);
                    
                    //Invoke start() method for thread
                    listener.start();
                    txtAreaOutputAppendAtLast(clientSocket + ": is in connection. ");
                }
                
            } catch (Exception e) {
            	
                txtAreaOutputAppendAtLast("Error making a connection. ");
            }
        }
    }
    
    public class CancelableReader extends BufferedReader {

        private final ExecutorService executor;
        private Future future;

        public CancelableReader(Reader in) {
            super(in);
            executor = Executors.newSingleThreadExecutor();
        }

        @Override
        public String readLine() {

            future = executor.submit(super::readLine);

            try {
                return (String) future.get();
            } catch (Exception e) {
                return null;
            }

        }

        public void cancelRead() {
        	executor.shutdown();
        	future.cancel(true);
        }

    }
    
    public class ClientHandler implements Runnable {
    	
    	Socket socket;
        CancelableReader  netin;       	
        PrintWriter netout;
        
        boolean stopFlag = false;
        
        String clientIP = "";
        String gunName = "";
        String softwareVersion = "";
        String deviceType = "";
        
        //Constructor
        public ClientHandler(Socket clientSocket) {
        	
            try {
            	
            	socket = clientSocket;    
            	
                //Get client IP Address
                clientIP = socket.getInetAddress().getHostAddress();
                
                Socket tempSocket;            	
                for(int i = 0; i < clientSockets.size(); i++) {
                	tempSocket = clientSockets.get(i);
                	if(tempSocket.getInetAddress().getHostAddress().equals(clientIP)) {
                		tableModel.removeRow(i);
                		clientSockets.remove(tempSocket);
                		txtAreaOutputAppendAtLast(clientIP + " - Disconnected.");
                		
                		tempSocket.close();
    					clientHandlers.get(i).netin.cancelRead();
    					clientHandlers.remove(i);
    					
    					/*for(Thread t : Thread.getAllStackTraces().keySet()) {
        					if(t.getId() == threads.get(i).getId()) {
        						threads.remove(i);
            					t.interrupt();
        						break;
            			    }
            			}*/
                	}
                	
                }
                
                //Add a client socket in the vector named clientSockets
                clientSockets.add(socket);
                
                //Get a input stream of the client socket
                netin = new CancelableReader(new BufferedReader(new InputStreamReader(socket.getInputStream())));
                
                //Get a output stream of the client socket
                netout = new PrintWriter(socket.getOutputStream());

                
            } catch (Exception e) {
                txtAreaOutputAppendAtLast("Unexpected error... " + e);
            }

        }
        
        public void run() {
        	
            String message;            
            try {
            	
            	while ((message = netin.readLine())!=null) {            		
            		
            		String[] data = message.split("&");
            		
            		Vector<Object> tableRow = new Vector<Object>();

                    if(data[0].equals("connection")) {
                    	
                    	gunName 		= data[1];
                        softwareVersion = data[2];
                        deviceType 		= data[3];
                        
                    	tableRow.add(clientIP);
                        tableRow.add(gunName);
                        tableRow.add(softwareVersion);
                        tableRow.add((new java.util.Date()).toString());
                        tableRow.add(deviceType);
                        tableRow.add("");
                        
                        // Add a client row into the table
                        tableModel.addRow(tableRow);
                       
                        txtAreaOutputAppendAtLast(clientIP + " - Connected.");
                        
                    } else if (data[0].equals("disconnection")) {
                    	
                    	tableModel.removeRow(clientSockets.indexOf(socket));
                    	clientSockets.remove(socket);
                    			
                        txtAreaOutputAppendAtLast(clientIP + " - Disconnected.");
                    	
                    } else if (data[0].equals("message")) {
                        
                    	txtAreaOutputAppendAtLast("Data:" + Thread.currentThread().getId() + message);
                		tableModel.setValueAt(data[1], clientSockets.indexOf(socket), 5);
                        
                        txtAreaOutputAppendAtLast("RX - " + clientIP + " - " + Thread.currentThread().getId() + "-" + data[1] + "");
                        
                        /*SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = dateformat.format(new java.util.Date());
                        
                        String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, receivemessage, receiveat)" + 
                        			   " VALUES('" + clientIP + "', '" + gunName + "', '" + softwareVersion + "', '" + deviceType + "', '" + data[1] + "', '" + (date) + "')";
                        dbStatement.executeUpdate(query);*/
                        
                        //netout.println(data[1]);
                        //netout.flush();
            			
                    }
            	}
            	
            } catch (Exception ex) {
            	 txtAreaOutputAppendAtLast("Run Exception:" + Thread.currentThread().getId() + "-" + ex);
				
            	 //tableModel.removeRow(clientSockets.indexOf(socket));
     			 //clientSockets.remove(socket);     			
     			 //txtAreaOutputAppendAtLast(clientIP + " - Disconnected.");
            }
        }
    }
    
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblMain = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableClients = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAreaOutput = new javax.swing.JTextArea();
        btnClearLog = new javax.swing.JButton();
        btnSendtoSelect = new javax.swing.JButton();
        btnSendtoAll = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblMain.setFont(new java.awt.Font("Sitka Small", 1, 28)); // NOI18N
        lblMain.setText("Laser Tag Server System");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(lblMain, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(lblMain)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tableClients.setAutoCreateRowSorter(true);
		tableClients.setRowSelectionAllowed(true);
		tableModel = new DefaultTableModel(0,0){

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

        };
        
        String header[] = new String[] { "IP Address", "Gun Name", "Software", "Last Seen", "Device Type", "Message Received" };
        tableModel.setColumnIdentifiers(header);
        tableClients.setModel(tableModel);
        
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuMessage1 = new JMenuItem("Message1");
        JMenuItem menuMessage2 = new JMenuItem("Message2");
        JMenuItem menuMessage3 = new JMenuItem("Message3");
         
        popupMenu.add(menuMessage1);
        popupMenu.add(menuMessage2);
        popupMenu.add(menuMessage3);
        
        tableClients.setComponentPopupMenu(popupMenu);	
        
        menuMessage1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	menuSendMesage(evt.getActionCommand());
            }
        });
        
        menuMessage2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	menuSendMesage(evt.getActionCommand());
            }
        });
        
        menuMessage3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	menuSendMesage(evt.getActionCommand());
            }
        });
        
        tableClients.setRowSelectionAllowed(true);
        tableClients.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tableClients);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 950, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setText("1st Value to Send:");

        jLabel2.setText("2nd Value to Send:");

        jLabel3.setText("3rd Value to Send:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Communication Log:");

        txtAreaOutput.setColumns(20);
        txtAreaOutput.setRows(5);
        jScrollPane2.setViewportView(txtAreaOutput);

        btnClearLog.setText("Clear Log");
        btnClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearLogActionPerformed(evt);
            }
        });

        btnSendtoSelect.setText("Send to Selected Gun");
        btnSendtoSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendtoSelectActionPerformed(evt);
            }
        });

        btnSendtoAll.setText("Send to All Gun");
        btnSendtoAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendtoAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(31, 31, 31)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField1)
                            .addComponent(jTextField2)
                            .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnSendtoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                        .addComponent(btnSendtoAll, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearLog, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(125, 125, 125))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClearLog, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSendtoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSendtoAll, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 25, Short.MAX_VALUE))
        );

        pack();
    }
    
    private void txtAreaOutputAppendAtLast(String newText) {
    	
    	txtAreaOutput.setForeground(Color.BLUE);
    	
    	String oldText = txtAreaOutput.getText();
        txtAreaOutput.setText("");
        txtAreaOutput.append(newText + "\n");
        txtAreaOutput.append(oldText);
        
    }
    
    private void btnSendtoSelectActionPerformed(java.awt.event.ActionEvent evt) {
        
        try {
    		
        	String firstVal  = jTextField1.getText();
        	String secondVal = jTextField2.getText();
        	String thirdVal  = jTextField3.getText();

        	int[] selectedrows = tableClients.getSelectedRows();
            
            for (int rowIndex = 0; rowIndex < selectedrows.length; rowIndex++) {

                Socket socket = clientSockets.get(selectedrows[rowIndex]);
            	PrintWriter netout = new PrintWriter(socket.getOutputStream());

            	String clientIP = socket.getInetAddress().getHostAddress();
            	txtAreaOutputAppendAtLast("TX - " + clientIP + " - " + firstVal + " " + secondVal + " " + thirdVal);

                /*SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateformat.format(new java.util.Date());
                
                String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, sendmessage, sendat)" + 
                			   " VALUES('" + clientIP + "', '', '', '', '" + firstVal + " " + secondVal + " " + thirdVal + "', '" + (date) + "')";
                dbStatement.executeUpdate(query);*/
                
				/*
                JSONArray arrJSON = new JSONArray();
				arrJSON.add(firstVal);
				arrJSON.add(secondVal);
				arrJSON.add(thirdVal);
				netout.println(arrJSON.toJSONString());
                netout.flush();*/
				
				netout.println(firstVal + "&" + secondVal + "&" + thirdVal);
				netout.flush();
                
                
            }
    		
    	} catch (Exception e) {
    		
    	}
    }

    private void btnSendtoAllActionPerformed(java.awt.event.ActionEvent evt) {

    	try {
    		
    		for(int rowIndex = 0; rowIndex < clientSockets.size(); rowIndex++) {
                Socket socket = clientSockets.get(rowIndex);
                PrintWriter netout = new PrintWriter(socket.getOutputStream());

                String firstVal = jTextField1.getText();
                String secondVal = jTextField2.getText();
                String thirdVal = jTextField3.getText();

            	String clientIP = socket.getInetAddress().getHostAddress();
            	txtAreaOutputAppendAtLast("TX - EVERYONE - " + firstVal + " " + secondVal + " " + thirdVal);

                /*SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateformat.format(new java.util.Date());
                
                String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, sendmessage, sendat)" + 
                			   " VALUES('" + clientIP + "', '', '', '', '" + firstVal + " " + secondVal + " " + thirdVal + "', '" + (date) + "')";
                dbStatement.executeUpdate(query);*/

				/*JSONArray arrJSON = new JSONArray();
				arrJSON.add(firstVal);
				arrJSON.add(secondVal);
				arrJSON.add(thirdVal);
				netout.println(arrJSON.toJSONString());
				netout.flush();*/
                
                netout.println(firstVal + "&" + secondVal + "&" + thirdVal);
				netout.flush();
            }
    	} catch (Exception e) {}
    }

    private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {
        
        txtAreaOutput.setText("");
        
    }

    private void menuSendMesage(String message) {
        
    	try {
    		
        	int[] selectedrows = tableClients.getSelectedRows();
            
            for (int rowIndex = 0; rowIndex < selectedrows.length; rowIndex++) {

                Socket socket = clientSockets.get(selectedrows[rowIndex]);
            	PrintWriter netout = new PrintWriter(socket.getOutputStream());
            	
            	String clientIP = socket.getInetAddress().getHostAddress();
            	txtAreaOutputAppendAtLast("TX - " + clientIP + " - " + message);
            	
                /*SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateformat.format(new java.util.Date());
                
                String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, sendmessage, sendat)" + 
                			   " VALUES('" + clientIP + "', '', '', '', '" + message + "', '" + (date) + "')";
                dbStatement.executeUpdate(query);*/
                
				/*JSONArray arrJSON = new JSONArray();
				arrJSON.add(message);				
				netout.println(arrJSON.toJSONString());
                netout.flush();*/
                
                netout.println(message);
                netout.flush();
            }
    		
    	} catch (Exception e) {
    		
    	}
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
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	
            	Server server = new Server();
            	server.setVisible(true);
				
            	//Login login = new Login(server);
            	
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearLog;
    private javax.swing.JButton btnSendtoAll;
    private javax.swing.JButton btnSendtoSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JLabel lblMain;
    public static javax.swing.JTable tableClients;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem popupMenuItem;
    public static javax.swing.JTextArea txtAreaOutput;
    // End of variables declaration//GEN-END:variables
}

class Login extends JFrame{
	private Server server;
	
	private javax.swing.JButton btnForgotPassword;
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUserName;
    
	Login(Server server) {
		
		this.server = server;
		
		initComponents();

	}

	private void initComponents() {
		
	    jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btnLogin = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField("ummi");
        txtUserName = new javax.swing.JTextField("ummi");
        btnForgotPassword = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel1.setFont(new java.awt.Font("Sitka Small", 1, 28)); // NOI18N
        jLabel1.setText("Login");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(178, 178, 178)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(59, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        btnLogin.setText("Login");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        jLabel2.setText("Username:");

        jLabel3.setText("Password:");

        btnForgotPassword.setText("Forgot Password");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnForgotPassword)
                        .addGap(3, 3, 3))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(72, 72, 72)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                            .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(114, 114, 114))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnForgotPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(185, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setVisible(true);
	}
	
	public void btnLoginActionPerformed(ActionEvent evt) {
		
		try{
			
			String username = txtUserName.getText();
			String password = String.valueOf(txtPassword.getPassword());
			
			if (username.equals("") || password.equals("")) return;
			
			ResultSet rs = this.server.dbStatement.executeQuery("SELECT * FROM users");
			
			while(rs.next()) {
				
				String dbUserName = rs.getString("username");
				String dbPassword = rs.getString("password");
				
				if(username.equals(dbUserName) && 
				   password.equals(dbPassword)) {
					
					this.server.setVisible(true);
					setVisible(false);
				}
			}
			
		} catch(Exception e) {System.out.println(e);}		
		
	}
}
