package Connection;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class Server extends javax.swing.JFrame{
    
    ArrayList<PrintWriter> clientOutputStreams;
    ArrayList<String> users;
    Vector<String> clientdata = new Vector<String>();
    Vector<Socket> clientSockets = new Vector<Socket>();
    Socket sock;
    String clientIP = "";
    
    DefaultTableModel model;
    
    Statement stmt;

    public class ClientHandler implements Runnable{
    	InputStreamReader isReader;
        BufferedReader reader;
        PrintWriter client;
        String clientName = "";
        String gunName = "";
        String softVersion = "";
        String devType = "";
        
        //Constructor
        public ClientHandler(Socket clientSocket, PrintWriter user) 
       {
            client = user;
            try 
            {
                sock = clientSocket;
                
                //Get client IP Address
                clientIP = sock.getInetAddress().getHostAddress();
                
                clientSockets.add(clientSocket);
                //get input from server -> client
                isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
                
                clientName = isReader.toString();
                //System.out.println(clientName);
                clientdata.add(clientName);
            }
            catch (Exception ex) 
            {
                txtAreaOutputAppendAtLast("Unexpected error... " + ex);
            }

       }

        @Override
        public void run() {
            String message; //connect = "Connect", disconnect = "Disconnect";
            String[] data;
            
            try 
            {
                while ((message = reader.readLine()) != null) 
                {
                    data = message.split("&");
                    
                    Vector<Object> rowData = new Vector<Object>();

                    if(data[0].equals("connection")) {
                    	
                        gunName = data[1];
                        softVersion = data[2];
                        devType = data[3];
                        
                        rowData.add(clientIP);
                        rowData.add(gunName);
                        rowData.add(softVersion);
                        rowData.add((new java.util.Date()).toString());
                        rowData.add(devType);
                        rowData.add("");
                        
                        // Add row client into the table
                        model.addRow(rowData);
                       
                        txtAreaOutputAppendAtLast(clientIP + " - Connected.");
                        
                    } else if (data[0].equals("disconnection")) {
                    	
                    	for (int i = 0; i < clientdata.size(); i++) {
                            if(clientName.equals(clientdata.get(i))) {
                        	model.removeRow(i);
                        	clientdata.remove(i);
                        	clientSockets.remove(i);
                        	break;
                            }
                            txtAreaOutputAppendAtLast(clientIP + " - Disconnected.");
                        }
                    	
                    } else if (data[0].equals("message")) {
                        
                        for (int i = 0; i < clientdata.size(); i++) {
                        	if(clientName.equals(clientdata.get(i))) {
                        		model.setValueAt(data[1], i, 5);
                        		break;
                        	}
                        }
                        
                        txtAreaOutputAppendAtLast("RX - " + clientIP + " - " + data[1] + "");
                        
                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = dateformat.format(new java.util.Date());
                        
                        String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, receivemessage, receiveat)" + 
                        			   " VALUES('" + clientIP + "', '" + gunName + "', '" + softVersion + "', '" + devType + "', '" + data[1] + "', '" + (date) + "')";
                        stmt.executeUpdate(query);
            			
                    }
                    
                } 
             } 
             catch (Exception ex) 
             {
                //txtAreaOutput.append("Lost a connection. ");
                ex.printStackTrace();
                clientOutputStreams.remove(client);
             } 
        }
        
    }
    
    private void txtAreaOutputAppendAtLast(String newText) {
    	
    	txtAreaOutput.setForeground(Color.BLUE);
    	
    	String oldText = txtAreaOutput.getText();
        txtAreaOutput.setText("");
        txtAreaOutput.append(newText + "\n");
        txtAreaOutput.append(oldText);
        
    }
    
    public Server() {
    	
    	initComponents();
        
        try
        {
        	Class.forName("com.mysql.jdbc.Driver");  
    		Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/lasertagdb","root","");  
    		stmt=con.createStatement();  
    		
    		Thread starter = new Thread(new ServerStart());
            starter.start();
        
            btnSendtoSelect.setEnabled(true);
            btnSendtoAll.setEnabled(true);
        
            txtAreaOutputAppendAtLast("Server started...");
            
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblMain = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listClients = new javax.swing.JTable();
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

        listClients.setAutoCreateRowSorter(true);
		listClients.setRowSelectionAllowed(true);
		model = new DefaultTableModel(0,0){

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

        };
        
        String header[] = new String[] { "IP Address", "Gun Name", "Software", "Last Seen", "Device Type", "Message Received" };
        model.setColumnIdentifiers(header);
        listClients.setModel(model);
        
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuMessage1 = new JMenuItem("Message1");
        JMenuItem menuMessage2 = new JMenuItem("Message2");
        JMenuItem menuMessage3 = new JMenuItem("Message3");
         
        popupMenu.add(menuMessage1);
        popupMenu.add(menuMessage2);
        popupMenu.add(menuMessage3);
        
        listClients.setComponentPopupMenu(popupMenu);	
        
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
        
        //Event after right click
        /*listClients.addMouseListener(new MouseAdapter(){
            //Create right menu pop up menu
            final RowMenu rightMenu = new RowMenu(listClients);
            @Override
            public void mouseClicked(MouseEvent me){
                //determine if right clicked
                int r = listClients.rowAtPoint(me.getPoint());
                if(r >= 0 && r < listClients.getRowCount()){
                    listClients.setRowSelectionInterval(r, r);
                } else {
                    listClients.clearSelection();
                }
                
                int rowindex = listClients.getSelectedRow();
                if(rowindex < 0){
                    return;
                }                
                if(me.isPopupTrigger() && me.getComponent() instanceof JTable)
                {
                    //JPopupMenu rightMenu = new RowMenu(listClients);
                    //rightMenu.show(me.getComponent(), me.getX(), me.getY()); 
                }
            }
        
        });*/
        
        listClients.setRowSelectionAllowed(true);
        listClients.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(listClients);

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
    
    private void menuSendMesage(String message) {
        
    	try {
    		
        	int[] selectedrows = listClients.getSelectedRows();
            
            for (int rowIndex = 0; rowIndex < selectedrows.length; rowIndex++) {

                Socket socket = clientSockets.get(selectedrows[rowIndex]);
            	PrintWriter writer = new PrintWriter(socket.getOutputStream());
            	
            	String clientIP = socket.getInetAddress().getHostAddress();
            	txtAreaOutputAppendAtLast("TX - " + clientIP + " - " + message);
            	
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateformat.format(new java.util.Date());
                
                String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, sendmessage, sendat)" + 
                			   " VALUES('" + clientIP + "', '', '', '', '" + message + "', '" + (date) + "')";
                stmt.executeUpdate(query);

                
            	writer.println(message);
                writer.flush();
                
           }
    		
    	} catch (Exception e) {
    		
    	}
    }
    
    private void btnSendtoSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendtoSelectActionPerformed
        // TODO add your handling code here:
        try {
    		
        	String firstVal = jTextField1.getText();
        	String secondVal = jTextField2.getText();
        	String thirdVal = jTextField3.getText();

        	int[] selectedrows = listClients.getSelectedRows();
            
            for (int rowIndex = 0; rowIndex < selectedrows.length; rowIndex++) {

                Socket socket = clientSockets.get(selectedrows[rowIndex]);
            	PrintWriter writer = new PrintWriter(socket.getOutputStream());

            	String clientIP = socket.getInetAddress().getHostAddress();
            	txtAreaOutputAppendAtLast("TX - " + clientIP + " - " + firstVal + " " + secondVal + " " + thirdVal);

                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateformat.format(new java.util.Date());
                
                String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, sendmessage, sendat)" + 
                			   " VALUES('" + clientIP + "', '', '', '', '" + firstVal + " " + secondVal + " " + thirdVal + "', '" + (date) + "')";
                stmt.executeUpdate(query);
                
            	writer.println(firstVal + "&" + secondVal + "&" + thirdVal);
                writer.flush();
                
           }
    		
    	} catch (Exception e) {
    		
    	}
    }//GEN-LAST:event_btnSendtoSelectActionPerformed

    private void btnSendtoAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendtoAllActionPerformed
        // TODO add your handling code here:
        try {
    		
    		for(int rowIndex = 0; rowIndex < clientSockets.size(); rowIndex++) {
                Socket socket = clientSockets.get(rowIndex);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());

                String firstVal = jTextField1.getText();
                String secondVal = jTextField2.getText();
                String thirdVal = jTextField3.getText();

            	String clientIP = socket.getInetAddress().getHostAddress();
            	txtAreaOutputAppendAtLast("TX - EVERYONE - " + firstVal + " " + secondVal + " " + thirdVal);

                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateformat.format(new java.util.Date());
                
                String query = "INSERT INTO messages(ipaddress, gunname, softversion, devtype, sendmessage, sendat)" + 
                			   " VALUES('" + clientIP + "', '', '', '', '" + firstVal + " " + secondVal + " " + thirdVal + "', '" + (date) + "')";
                stmt.executeUpdate(query);

            	writer.println(firstVal + "&" + secondVal + "&" + thirdVal);
                writer.flush();
            }
    	} catch (Exception e) {}
    }//GEN-LAST:event_btnSendtoAllActionPerformed

    private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearLogActionPerformed
        // TODO add your handling code here:
        txtAreaOutput.setText("");
        
    }//GEN-LAST:event_btnClearLogActionPerformed
/**
    private void listClientsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listClientsMouseClicked

    }//GEN-LAST:event_listClientsMouseClicked
*/
    private void popupMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_popupMenuMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_popupMenuMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        //</editor-fold>
        
        

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	Server server = new Server();
            	Login login = new Login(server);
            	
            	//server.setVisible(true);
            }
        });
    }
    
     public class ServerStart implements Runnable 
    {
        @Override
        public void run() 
        {
            clientOutputStreams = new ArrayList();
            users = new ArrayList();  

            try 
            {
                //Server is listening on port 2222
                ServerSocket serverSock = new ServerSocket(2222);
                
                //running infinite loop for getting client request
                while (true) 
                {
                    //Accept a new connection
                    Socket clientSock = serverSock.accept();
                    
                    //Obtaining input and output streams
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);

                    //Create a new thread object
                    Thread listener = new Thread(new ClientHandler(clientSock, writer));
                    //Invoke start() method for thread
                    listener.start();
                    txtAreaOutputAppendAtLast(clientSock + ": is in connection. ");
                }
            }
            catch (Exception ex)
            {
                txtAreaOutputAppendAtLast("Error making a connection. ");
            }
        }
    }
     
    class RowMenu 
    {
        public RowMenu(JTable listClients)
        {
            //Create right click menu
            JMenuItem add = new JMenuItem("Add");
            JMenuItem edit = new JMenuItem("Edit");
            JMenuItem delete = new JMenuItem("Delete");
            
            //When Add Clicked
            add.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0){
                    JOptionPane.showMessageDialog(add, "ADDED");
                }
            });
            
            //When Edit Clicked
            edit.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0){
                    JOptionPane.showMessageDialog(add, "EDITED");
                }
            });
            
            //When Delete Clicked
            delete.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0){
                    JOptionPane.showMessageDialog(add, "DELETED");
                }
            });
            
            //Add Items to Pop-up Menu
            add(add);
            add(edit);
            add(new JSeparator());
            add(delete);
        }
    
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
    public static javax.swing.JTable listClients;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem popupMenuItem;
    public static javax.swing.JTextArea txtAreaOutput;
    // End of variables declaration//GEN-END:variables
}

class Login implements ActionListener{
	private JTextField txtUserName;
	private JPasswordField txtPassword;
	private Server server;
	
	JFrame frame;
	
	Login(Server server) {
		
		this.server = server;

		frame = new JFrame("User Login");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.add(panel);
		initComponents(panel);

		frame.setVisible(true);
	}

	private void initComponents(JPanel panel) {

		panel.setLayout(null);

		JLabel lblUserName = new JLabel("User");
		lblUserName.setBounds(10, 10, 80, 25);
		panel.add(lblUserName);

		txtUserName = new JTextField(20);
		txtUserName.setBounds(100, 10, 160, 25);
		panel.add(txtUserName);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(10, 40, 80, 25);
		panel.add(lblPassword);

		txtPassword = new JPasswordField(20);
		txtPassword.setBounds(100, 40, 160, 25);
		panel.add(txtPassword);

		JButton loginButton = new JButton("login");
		loginButton.setBounds(10, 80, 80, 25);
		loginButton.addActionListener(this);
		panel.add(loginButton);
		
		JButton registerButton = new JButton("register");
		registerButton.setBounds(180, 80, 80, 25);
		panel.add(registerButton);
	}
	
	public void actionPerformed(ActionEvent evt) {
		
		try{
			
			String username = txtUserName.getText();
			String password = String.valueOf(txtPassword.getPassword());
			
			if (username.equals("") || password.equals("")) return;
			
			ResultSet rs = this.server.stmt.executeQuery("SELECT * FROM users");
			
			while(rs.next()) {
				
				String dbUserName = rs.getString("username");
				String dbPassword = rs.getString("password");
				
				if(username.equals(dbUserName) && 
				   password.equals(dbPassword)) {
					
					this.server.setVisible(true);
					frame.setVisible(false);
				}
			}
			
		} catch(Exception e){ System.out.println(e);}		
		
	}
}
