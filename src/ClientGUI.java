import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ClientGUI {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextArea textArea;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ClientGUI window = new ClientGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ClientGUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("通讯录系统 - 客户端");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel homePanel = createHomePanel();
        JPanel addPanel = createAddPanel();
        JPanel showPanel = createShowPanel();
        JPanel modifyPanel = createModifyPanel();
        JPanel findPanel = createFindPanel();

        mainPanel.add(homePanel, "Home");
        mainPanel.add(addPanel, "Add");
        mainPanel.add(showPanel, "Show");
        mainPanel.add(modifyPanel, "Modify");
        mainPanel.add(findPanel, "Find");

        frame.getContentPane().add(mainPanel);
        cardLayout.show(mainPanel, "Home");
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2, 10, 10));

        JButton btnAdd = new JButton("增加联系人");
        btnAdd.setBounds(50,50,200,100);
        btnAdd.addActionListener(e -> cardLayout.show(mainPanel, "Add"));
        panel.add(btnAdd);

        JButton btnShow = new JButton("显示联系人");
        btnShow.addActionListener(e -> {
            String response = sendToServer("SHOW");
            textArea.setText(response);
            cardLayout.show(mainPanel, "Show");
        });
        panel.add(btnShow);

        JButton btnModify = new JButton("修改联系人");
        btnModify.addActionListener(e -> cardLayout.show(mainPanel, "Modify"));
        panel.add(btnModify);

        JButton btnFind = new JButton("查找联系人");
        btnFind.addActionListener(e -> cardLayout.show(mainPanel, "Find"));
        panel.add(btnFind);

        return panel;
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JComboBox<String> sexBox = new JComboBox<>(new String[]{"男", "女"});
        JTextField phoneField = new JTextField();
        JTextField addrField = new JTextField();
        JTextField ipField = new JTextField();

        panel.add(new JLabel("姓名:"));
        panel.add(nameField);

        panel.add(new JLabel("年龄:"));
        panel.add(ageField);

        panel.add(new JLabel("性别:"));
        panel.add(sexBox);

        panel.add(new JLabel("电话:"));
        panel.add(phoneField);

        panel.add(new JLabel("地址:"));
        panel.add(addrField);

        panel.add(new JLabel("IP地址"));
        panel.add(ipField);

        JButton btnAdd = new JButton("添加");
        btnAdd.addActionListener(e -> {
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String sex = (String) sexBox.getSelectedItem();
            String phone = phoneField.getText();
            String addr = addrField.getText();
            String ip = ipField.getText();
            String msg = String.format("ADD;%s;%d;%s;%s;%s;%s", name, age, sex, phone, addr,ip);
            String response = sendToServer(msg);
            JOptionPane.showMessageDialog(frame, response, "添加结果", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(btnAdd);

        JButton btnBack = new JButton("返回");
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        panel.add(btnBack);

        return panel;
    }


    private JPanel createShowPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton btnBack = new JButton("返回");
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        panel.add(btnBack, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createModifyPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));

        JTextField oldNameField = new JTextField();
        JTextField newNameField = new JTextField();
        JTextField ageField = new JTextField();
        JComboBox<String> sexBox = new JComboBox<>(new String[]{"男", "女"});
        JTextField phoneField = new JTextField();
        JTextField addrField = new JTextField();
        JTextField ipField = new JTextField();

        panel.add(new JLabel("旧姓名:"));
        panel.add(oldNameField);

        panel.add(new JLabel("新姓名:"));
        panel.add(newNameField);

        panel.add(new JLabel("年龄:"));
        panel.add(ageField);

        panel.add(new JLabel("性别:"));
        panel.add(sexBox);

        panel.add(new JLabel("电话:"));
        panel.add(phoneField);

        panel.add(new JLabel("地址:"));
        panel.add(addrField);

        panel.add(new JLabel("Ip地址:"));
        panel.add(ipField);

        JButton btnModify = new JButton("修改");
        btnModify.addActionListener(e -> {
            String oldName = oldNameField.getText();
            String newName = newNameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String sex = (String) sexBox.getSelectedItem();
            String phone = phoneField.getText();
            String addr = addrField.getText();
            String ip = ipField.getText();
            String msg = String.format("MODIFY;%s;%s;%d;%s;%s;%s;%s", oldName, newName, age, sex, phone, addr, ip);
            String response = sendToServer(msg);
            JOptionPane.showMessageDialog(frame, response, "修改结果", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(btnModify);

        JButton btnBack = new JButton("返回");
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        panel.add(btnBack);

        return panel;
    }


    private JPanel createFindPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        JTextField nameField = new JTextField();
        panel.add(new JLabel("姓名:"));
        panel.add(nameField);

        JButton btnFind = new JButton("查找");
        btnFind.addActionListener(e -> {
            String name = nameField.getText();
            String msg = String.format("FIND;%s", name);
            String response = sendToServer(msg);
            if (response.startsWith("未找到")) {
                JOptionPane.showMessageDialog(frame, response, "查找结果", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String[] fields = response.split("\t");
                StringBuilder formattedResponse = new StringBuilder("<html>");
                for (String field : fields) {
                    formattedResponse.append(field).append("<br>");
                }
                formattedResponse.append("</html>");
                JOptionPane.showMessageDialog(frame, formattedResponse.toString(), "查找结果", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panel.add(btnFind);

        JButton btnBack = new JButton("返回");
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        panel.add(btnBack);

        return panel;
    }
    private String sendToServer(String message) {
        try (Socket socket = new Socket("192.168.192.51", 4869);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(message);
            StringBuilder sb = new StringBuilder();
            String response;
            while ((response = in.readLine()) != null) {
                sb.append(response).append("\n");
            }
            return sb.toString().trim();

        } catch (IOException e) {
            e.printStackTrace();
            return "通信错误";
        }
    }

}
