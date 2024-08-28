//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.*;
//import java.net.Socket;
//
//public class ClientGUI {
//    private JFrame frame;
//    private JTextArea textArea;
//    private JTextField nameField, ageField, phoneField, addrField;
//    private JComboBox<String> sexBox;
//
//    public static void main(String[] args) {
//        EventQueue.invokeLater(() -> {
//            try {
//                ClientGUI window = new ClientGUI();
//                window.frame.setVisible(true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    public ClientGUI() {
//        initialize();
//    }
//
//    private void initialize() {
//        frame = new JFrame("通讯录系统");
//        frame.setBounds(100, 100, 500, 400);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.getContentPane().setLayout(null);
//
//        JLabel lblName = new JLabel("姓名:");
//        lblName.setBounds(10, 20, 50, 15);
//        frame.getContentPane().add(lblName);
//
//        nameField = new JTextField();
//        nameField.setBounds(60, 20, 100, 20);
//        frame.getContentPane().add(nameField);
//        nameField.setColumns(10);
//
//        JLabel lblAge = new JLabel("年龄:");
//        lblAge.setBounds(180, 20, 50, 15);
//        frame.getContentPane().add(lblAge);
//
//        ageField = new JTextField();
//        ageField.setBounds(230, 20, 50, 20);
//        frame.getContentPane().add(ageField);
//        ageField.setColumns(10);
//
//        JLabel lblSex = new JLabel("性别:");
//        lblSex.setBounds(300, 20, 50, 15);
//        frame.getContentPane().add(lblSex);
//
//        sexBox = new JComboBox<>(new String[]{"男", "女"});
//        sexBox.setBounds(350, 20, 60, 20);
//        frame.getContentPane().add(sexBox);
//
//        JLabel lblPhone = new JLabel("电话:");
//        lblPhone.setBounds(10, 60, 50, 15);
//        frame.getContentPane().add(lblPhone);
//
//        phoneField = new JTextField();
//        phoneField.setBounds(60, 60, 100, 20);
//        frame.getContentPane().add(phoneField);
//        phoneField.setColumns(10);
//
//        JLabel lblAddr = new JLabel("地址:");
//        lblAddr.setBounds(180, 60, 50, 15);
//        frame.getContentPane().add(lblAddr);
//
//        addrField = new JTextField();
//        addrField.setBounds(230, 60, 180, 20);
//        frame.getContentPane().add(addrField);
//        addrField.setColumns(10);
//
//        JButton btnAdd = new JButton("增加联系人");
//        btnAdd.setBounds(40, 100, 150, 30);
//        btnAdd.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                String name = nameField.getText();
//                int age = Integer.parseInt(ageField.getText());
//                String sex = (String) sexBox.getSelectedItem();
//                String phone = phoneField.getText();
//                String addr = addrField.getText();
//                String msg = String.format("ADD;%s;%d;%s;%s;%s", name, age, sex, phone, addr);
//                sendToServer(msg);
//            }
//        });
//        frame.getContentPane().add(btnAdd);
//
//        JButton btnShow = new JButton("显示联系人");
//        btnShow.setBounds(220, 100, 150, 30);
//        btnShow.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                sendToServer("SHOW");
//            }
//        });
//        frame.getContentPane().add(btnShow);
//
//        JButton btnModify = new JButton("修改联系人");
//        btnModify.setBounds(40,150,150,30);
//        btnModify.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String oldName = nameField.getText();
//                String newName = nameField.getText();
//                int age = Integer.parseInt(ageField.getText());
//                String sex = (String) sexBox.getSelectedItem();
//                String phone = phoneField.getText();
//                String addr = addrField.getText();
//                String msg = String.format("MODIFY;%s;%s;%d;%s;%s;%s", oldName,newName, age, sex, phone, addr);
//                sendToServer(msg);
//            }
//        });
//        frame.getContentPane().add(btnModify);
//
//        JButton btnFind = new JButton("查找联系人");
//        btnFind.setBounds(220,150,150,30);
//        btnFind.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String name = nameField.getText();
//                String msg = String.format("Find;%s",name);
//                sendToServer(msg);
//            }
//        });
//        frame.getContentPane().add(btnFind);
//
//        JButton btnDelete = new JButton("删除联系人");
//        btnDelete.setBounds(40,200,150,30);
//        btnDelete.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String name = nameField.getText();
//                String msg = String.format("DELETE;%s",name);
//                sendToServer(msg);
//            }
//        });
//        frame.getContentPane().add(btnDelete);
//
//        JButton btnClean = new JButton("清空联系人");
//        btnClean.setBounds(220,200,150,30);
//        btnClean.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                sendToServer("CLEAN");
//            }
//        });
//        frame.getContentPane().add(btnClean);
//
//        textArea = new JTextArea();
//        textArea.setBounds(10, 150, 560, 200);
//        frame.getContentPane().add(textArea);
//    }
//
//    private void sendToServer(String message) {
//        try (Socket socket = new Socket("localhost", 5050);
//             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//            out.println(message);
//            String response;
//            textArea.setText("");
//            while ((response = in.readLine()) != null) {
//                textArea.append(response + "\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
