import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class People {
    String Name;
    int Age;
    int Sex;
    String Phone;
    String Addr;
    String IpAddress;

    People(String name, int age, int sex, String phone, String addr, String ipAddress) {
        this.Name = name;
        this.Age = age;
        this.Sex = sex;
        this.Phone = phone;
        this.Addr = addr;
        this.IpAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "姓名: " + Name + "\t" +
                "年龄: " + Age + "\t" +
                "性别: " + (Sex == 1 ? "男" : "女") + "\t" +
                "电话: " + Phone + "\t" +
                "住址: " + Addr + "\t" +
                "IP地址: " + IpAddress;
    }
}

class ContactBooks {
    final List<People> peopleList = new ArrayList<>();

    synchronized String addContact(People person) {
        if (!Validator.isValidPhone(person.Phone)) {
            return "无效的电话号码格式";
        }
        if (!Validator.isValidIPv4(person.IpAddress)) {
            return "无效的IP地址格式";
        }
        peopleList.add(person);
        saveContactsToFile();
        return "添加成功";
    }

    synchronized String showContacts() {
        if (peopleList.isEmpty()) {
            return "当前通讯录为空";
        } else {
            StringBuilder result = new StringBuilder();
            for (People p : peopleList) {
                result.append(p.toString()).append("\n");
            }
            return result.toString();
        }
    }

    synchronized String modifyContact(String oldName, People newPeople) {
        if (!Validator.isValidPhone(newPeople.Phone)) {
            return "无效的电话号码格式";
        }
        if (!Validator.isValidIPv4(newPeople.IpAddress)) {
            return "无效的IP地址格式";
        }
        for (People p : peopleList) {
            if (p.Name.equals(oldName)) {
                p.Name = newPeople.Name;
                p.Age = newPeople.Age;
                p.Sex = newPeople.Sex;
                p.Phone = newPeople.Phone;
                p.Addr = newPeople.Addr;
                p.IpAddress = newPeople.IpAddress;
                saveContactsToFile();
                return "修改完成";
            }
        }
        return "没有此联系人";
    }

    synchronized String findContact(String name) {
        for (People p : peopleList) {
            if (p.Name.equals(name)) {
                return p.toString();
            }
        }
        return "未找到此联系人";
    }

    synchronized String deleteContact(String name) {
        for (People p : peopleList) {
            if (p.Name.equals(name)) {
                peopleList.remove(p);
                saveContactsToFile();
                return "删除成功";
            }
        }
        return "未找到此联系人";
    }

    synchronized void cleanContact() {
        peopleList.clear();
        saveContactsToFile();
    }
    private void saveContactsToFile() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("contacts.txt")))) {
            writer.println("姓名    年龄   性别  电话          住址   IP地址");
            for (People p : peopleList) {
                if(p.Sex==1) {
                    writer.println(p.Name + "    " + p.Age + "     " + "男" + "   " + p.Phone + "  " + p.Addr + "   " + p.IpAddress);
                } else {
                    writer.println(p.Name + "    " + p.Age + "     " + "女" + "   " + p.Phone + "  " + p.Addr + "   " + p.IpAddress);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Validator {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
    );
    public static boolean isValidPhone(String phone) {
        Matcher matcher = PHONE_PATTERN.matcher(phone);
        return matcher.matches();
    }
    public static boolean isValidIPv4(String ip) {
        Matcher matcher = IPV4_PATTERN.matcher(ip);
        return matcher.matches();
    }
}

public class Server {
    private static final ContactBooks contactBooks = new ContactBooks();

    public static void main(String[] args) {
        // 启动服务器线程
        new Thread(Server::startServer).start();

        EventQueue.invokeLater(() -> {
            try {
                ClientGUI window = new ClientGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(4869)) {
            System.out.println("服务器启动...");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                //String clientIp = socket.getInetAddress().getHostAddress();

                String request = in.readLine();
                if (request != null) {
                    String[] parts = request.split(";");
                    String command = parts[0];
                    switch (command) {
                        case "ADD":
                            handleAdd(parts, out);
                            break;
                        case "SHOW":
                            handleShow(out);
                            break;
                        case "MODIFY":
                            handleModify(parts, out);
                            break;
                        case "FIND":
                            handleFind(parts, out);
                            break;
                        case "DELETE":
                            handleDelete(parts, out);
                            break;
                        case "CLEAN":
                            handleClean(out);
                            break;
                        default:
                            out.println("未知命令");
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleAdd(String[] parts, PrintWriter out) {
            if (parts.length != 7) {
                out.println("参数错误");
                return;
            }
            String name = parts[1];
            int age = Integer.parseInt(parts[2]);
            int sex = parts[3].equals("男") ? 1 : 2;
            String phone = parts[4];
            String addr = parts[5];
            String ip = parts[6];
            People people = new People(name, age, sex, phone, addr, ip);
            String result = contactBooks.addContact(people);
            System.out.println("Add result: " + result); // 调试信息
            out.println(result);
        }

        private void handleShow(PrintWriter out) {
            out.println(contactBooks.showContacts());
        }

        private void handleModify(String[] parts, PrintWriter out) {
            if (parts.length != 8) {
                out.println("参数错误");
                return;
            }
            String oldName = parts[1];
            String newName = parts[2];
            int newAge = Integer.parseInt(parts[3]);
            int newSex = parts[4].equals("男") ? 1 : 2;
            String newPhone = parts[5];
            String newAddr = parts[6];
            String newIp = parts[7];
            People existingContact = findExistingContact(oldName);
            if (existingContact != null) {
                String ip = newIp;
                People newPeople = new People(newName, newAge, newSex, newPhone, newAddr, ip);
                String result = contactBooks.modifyContact(oldName, newPeople);
                out.println(result);
            } else {
                out.println("未找到此联系人");
            }
        }

        private People findExistingContact(String name) {
            for (People p : contactBooks.peopleList) {
                if (p.Name.equals(name)) {
                    return p;
                }
            }
            return null;
        }

        private void handleFind(String[] parts, PrintWriter out) {
            if (parts.length != 2) {
                out.println("参数错误");
                return;
            }
            String findName = parts[1];
            out.println(contactBooks.findContact(findName));
        }

        private void handleDelete(String[] parts, PrintWriter out) {
            if (parts.length != 2) {
                out.println("参数错误");
                return;
            }
            String deleteName = parts[1];
            out.println(contactBooks.deleteContact(deleteName));
        }

        private void handleClean(PrintWriter out) {
            contactBooks.cleanContact();
            out.println("已经清空所有联系人");
        }
    }

    public static class ClientGUI {
        private JFrame frame;
        private CardLayout cardLayout;
        private JPanel mainPanel;
        private JTextArea textArea;

        public ClientGUI() {
            initialize();
        }

        private void initialize() {
            frame = new JFrame("通讯录系统 - 服务端");
            frame.setBounds(100, 100, 600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);

            JPanel homePanel = createHomePanel();
            JPanel addPanel = createAddPanel();
            JPanel showPanel = createShowPanel();
            JPanel modifyPanel = createModifyPanel();
            JPanel findPanel = createFindPanel();
            JPanel deletePanel = createDeletePanel();
            JPanel cleanPanel = createCleanPanel();

            mainPanel.add(homePanel, "Home");
            mainPanel.add(addPanel, "Add");
            mainPanel.add(showPanel, "Show");
            mainPanel.add(modifyPanel, "Modify");
            mainPanel.add(findPanel, "Find");
            mainPanel.add(deletePanel, "Delete");
            mainPanel.add(cleanPanel, "Clean");

            frame.getContentPane().add(mainPanel);
            cardLayout.show(mainPanel, "Home");
        }

        private JPanel createHomePanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 2, 10, 10));

            JButton btnAdd = new JButton("添加联系人");
            btnAdd.addActionListener(e -> cardLayout.show(mainPanel, "Add"));
            panel.add(btnAdd);

            JButton btnShow = new JButton("显示所有联系人");
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

            JButton btnDelete = new JButton("删除联系人");
            btnDelete.addActionListener(e -> cardLayout.show(mainPanel, "Delete"));
            panel.add(btnDelete);

            JButton btnClean = new JButton("清空所有联系人");
            btnClean.addActionListener(e -> {
                String response = sendToServer("CLEAN");
                JOptionPane.showMessageDialog(frame, response, "清空结果", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "Clean");
            });
            panel.add(btnClean);

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

            panel.add(new JLabel("IP地址:"));
            panel.add(ipField);

            JButton btnAdd = new JButton("添加");
            btnAdd.addActionListener(e -> {
                String name = nameField.getText();
                int age = Integer.parseInt(ageField.getText());
                String sex = (String) sexBox.getSelectedItem();
                String phone = phoneField.getText();
                String addr = addrField.getText();
                String ip = ipField.getText();
                String msg = String.format("ADD;%s;%d;%s;%s;%s;%s", name, age, sex, phone, addr, ip);
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

            panel.add(new JLabel("IP地址:"));
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


        private JPanel createDeletePanel() {
            JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

            JTextField nameField = new JTextField();
            panel.add(new JLabel("姓名:"));
            panel.add(nameField);

            JButton btnDelete = new JButton("删除");
            btnDelete.addActionListener(e -> {
                String name = nameField.getText();
                String msg = String.format("DELETE;%s", name);
                String response = sendToServer(msg);
                JOptionPane.showMessageDialog(frame, response, "删除结果", JOptionPane.INFORMATION_MESSAGE);
            });
            panel.add(btnDelete);

            JButton btnBack = new JButton("返回");
            btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
            panel.add(btnBack);

            return panel;
        }

        private JPanel createCleanPanel() {
            JPanel panel = new JPanel(new BorderLayout());

            JLabel lblMessage = new JLabel("通讯录已清空");
            lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(lblMessage, BorderLayout.CENTER);

            JButton btnBack = new JButton("返回");
            btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
            panel.add(btnBack, BorderLayout.SOUTH);

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
}
