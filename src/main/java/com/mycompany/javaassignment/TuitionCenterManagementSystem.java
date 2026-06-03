/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.javaassignment;

/**
 *
 * @author jer
 */
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.Month;
import javax.swing.table.DefaultTableModel;
import java.util.stream.IntStream;
import java.time.format.TextStyle;
import java.util.regex.Pattern;


//Main application
public class TuitionCenterManagementSystem {
    public static void main(String[] args) {
        syncStudentsToUsers();
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    private static void syncStudentsToUsers() {
        Set<String> existing = DataManager.loadAll("users.txt").stream()
            .map(r -> r[0])
            .collect(Collectors.toSet());

        for (String[] s : DataManager.loadAll("students.txt")) {
            String sid = s[0];

            if (!existing.contains(sid)) {
                DataManager.append("users.txt", new String[]{
                    sid,
                    "default123",
                    "Student",
                    sid,
                    "", // name placeholder
                    "", // IC
                    "", // email
                    "", // phone
                    ""  // address
                });
                
            }
        }
    }
}

//Data manager: whitespace-delimited TXT files
class DataManager {
    private static final String PATH = "data" + File.separator;

    public static List<String[]> loadAll(String file) {
        List<String[]> list = new ArrayList<>();
        Path p = Paths.get(PATH, file);
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                list.add(line.trim().split("\\s+"));
            }
        } catch (IOException e) {
            
        }
        return list;
    }

    public static void saveAll(String file, List<String[]> data) {
        Path p = Paths.get(PATH, file);
        try (BufferedWriter bw = Files.newBufferedWriter(p)) {
            for (String[] r : data) {
                bw.write(String.join(" ", r));
                bw.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void append(String file, String[] rec) {
        List<String[]> all = loadAll(file);
        all.add(rec);
        saveAll(file, all);
    }

    public static void update(String file, int keyCol, String key, String[] rec) {
    List<String[]> all = loadAll(file);
    for (int i = 0; i < all.size(); i++) {
        String[] row = all.get(i);
        if (row.length <= keyCol) continue;
        if (row[keyCol].equals(key)) {
            if (row.length != rec.length) {
                System.err.println("Column count changed for key " + key + " in " + file
                    + ": was " + row.length + " now " + rec.length + ", overwriting anyway.");
            }
            all.set(i, rec);
            break;
        }
    }
    saveAll(file, all);
}
    
    public static void delete(String file, int keyCol, String key) {
    List<String[]> all = loadAll(file).stream()
        .filter(r -> r.length > keyCol && !r[keyCol].equals(key))
        .collect(Collectors.toList());
    saveAll(file, all);
}
}

//Login screen
class LoginFrame extends JFrame {
    private JTextField tfUser = new JTextField(15);
    private JPasswordField pfPass = new JPasswordField(15);
    private int tries = 0;

    LoginFrame() {
        super("Genius Tuition Centre - Login");

        setLayout(new BorderLayout(20, 20));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Genius Tuition Centre", SwingConstants.CENTER);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 28f));
        add(lblTitle, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.LINE_END;

        gc.gridx = 0; gc.gridy = 0;
        center.add(new JLabel("Username:"), gc);
        gc.gridy = 1;
        center.add(new JLabel("Password:"), gc);

        gc.anchor = GridBagConstraints.LINE_START;
        gc.gridx = 1; gc.gridy = 0;
        center.add(tfUser, gc);
        gc.gridy = 1;
        center.add(pfPass, gc);

        add(center, BorderLayout.CENTER);

        JButton btnLogin = new JButton("Login");
        btnLogin.setPreferredSize(new Dimension(100, 30));
        btnLogin.addActionListener(e -> login());
        JPanel south = new JPanel();
        south.add(btnLogin);
        add(south, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void login() {
        List<String[]> users = DataManager.loadAll("users.txt");
        String u = tfUser.getText().trim();
        String p = new String(pfPass.getPassword());

        for (String[] r : users) {
            if (r.length >= 3 && r[0].equals(u) && r[1].equals(p)) {
                dispose();
                switch (r[2]) {
                    case "Admin": case "Receptionist": SwingUtilities.invokeLater(() -> new AdminReceptionistFrame(u)); return;
                    case "Tutor":    SwingUtilities.invokeLater(() -> new TutorFrame(u)); return;
                    case "Student":  SwingUtilities.invokeLater(() -> new StudentFrame(u)); return;
                    default:
                        JOptionPane.showMessageDialog(this, "Invalid user role.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
            }
        }

       tries++;
        if (tries >= 3) {
            JOptionPane.showMessageDialog(this, "Invalid (3/3)\nYou have exceeded the maximum number of attempts.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid (" + tries + "/3)", "Login Failed", JOptionPane.WARNING_MESSAGE);
        }
    }


//Admin / Receptionist main
class AdminReceptionistFrame extends JFrame {
    private static final Color BG_COLOR   = new Color(245, 245, 245);
    private static final Color BTN_COLOR  = new Color(70, 130, 180);
    private static final Color TXT_COLOR  = Color.WHITE;
    private static final Font  TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font  BTN_FONT   = new Font("SansSerif", Font.PLAIN, 14);

    public AdminReceptionistFrame(String user) {
        super("Genius Tuition Centre ‒ Admin/Receptionist Panel");
        String role = getRole(user);
        boolean isAdmin = "Admin".equalsIgnoreCase(role);
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored){}

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(BG_COLOR);
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setContentPane(root);

        JLabel lblTitle = new JLabel("Welcome, " + role, SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setBackground(BG_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        List<String> buttonLabels = new ArrayList<>();
        List<Runnable> buttonActions = new ArrayList<>();

        if (isAdmin) {
            buttonLabels.add("Receptionist Management");
            buttonActions.add(() -> new ReceptionistManagementDialog(this));
        }
        
        buttonLabels.add("Tutor Management");
        buttonActions.add(() -> new TutorManagementDialog(this));
        buttonLabels.add("Student Management");
        buttonActions.add(() -> new StudentManagementDialog(this));
        buttonLabels.add("Monthly Income Report");
        buttonActions.add(() -> new ReportDialog(this));
        buttonLabels.add("Add Payment");
        buttonActions.add(() -> new AddPaymentDialog(this, null));
        buttonLabels.add("Approve Subject Request");
        buttonActions.add(() -> new ApproveRequestsDialog(this));
        // Receptionist-only Profile in grid
        if (!isAdmin) {
            buttonLabels.add("Profile");
            buttonActions.add(() -> new ProfileDialog(this, user, role));
        }
        for (int i = 0; i < buttonLabels.size(); i++) {
            JButton btn = makeButton(buttonLabels.get(i), buttonActions.get(i));
            gc.gridx = i % 2;
            gc.gridy = i / 2;
            pnlButtons.add(btn, gc);
        }

        if (isAdmin) {
            //Profile Button Panel for Admin
            JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            profilePanel.setBackground(BG_COLOR);
            JButton btnProfile = makeButton("Profile", () -> new ProfileDialog(this, user, role));
            btnProfile.setPreferredSize(new Dimension(200, 40));
            profilePanel.add(btnProfile);

            JPanel centerBox = new JPanel();
            centerBox.setBackground(BG_COLOR);
            centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
            centerBox.add(pnlButtons);
            centerBox.add(profilePanel);
            root.add(centerBox, BorderLayout.CENTER);
        } else {
            // Receptionist: grid only
            root.add(pnlButtons, BorderLayout.CENTER);
        }

        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        pnlSouth.setBackground(BG_COLOR);
        JButton btnLogout = makeButton("Logout", () -> {
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new);
        });
        btnLogout.setPreferredSize(new Dimension(200, 40));
        btnLogout.setFont(BTN_FONT);
        pnlSouth.add(btnLogout);
        root.add(pnlSouth, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private String getRole(String user) {
        return DataManager.loadAll("users.txt").stream()
            .filter(r -> r[0].equals(user))
            .findFirst()
            .orElse(new String[]{"", "", ""})[2];
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(BTN_FONT);
        btn.setBackground(BTN_COLOR);
        btn.setForeground(TXT_COLOR);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 40));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}

//Tutor main
class TutorFrame extends JFrame {
    private static final Color BG_COLOR   = new Color(245, 245, 245);
    private static final Color BTN_COLOR  = new Color(70, 130, 180);
    private static final Color TXT_COLOR  = Color.WHITE;
    private static final Font  TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font  BTN_FONT   = new Font("SansSerif", Font.PLAIN, 14);

    TutorFrame(String user) {
        super("Genius Tuition Centre ‒ Tutor Panel");

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(BG_COLOR);
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setContentPane(root);

        JLabel lblTitle = new JLabel("Welcome, Tutor!", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setBackground(BG_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        String[] labels = {
            "Course Management",
            "View Students",
            "Change Charges",
            "Profile"
        };
        Runnable[] actions = {
            () -> new ScheduleManagementDialog(this, user),
            () -> new StudentViewDialog(this, user),
            () -> new ChangeChargesDialog(this, user),
            () -> new ProfileDialog(this, user, "Tutor")
        };

        for (int i = 0; i < labels.length; i++) {
            JButton btn = makeButton(labels[i], actions[i]);
            gc.gridx = i % 2;
            gc.gridy = i / 2;
            pnlButtons.add(btn, gc);
        }
        root.add(pnlButtons, BorderLayout.CENTER);

        JButton btnLogout = makeButton("Logout", () -> {
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new);
        });
        JPanel pnlSouth = new JPanel();
        pnlSouth.setBackground(BG_COLOR);
        pnlSouth.add(btnLogout);
        root.add(pnlSouth, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(BTN_FONT);
        btn.setBackground(BTN_COLOR);
        btn.setForeground(TXT_COLOR);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 40));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}

//Student main
class StudentFrame extends JFrame {
    private static final Color BG_COLOR   = new Color(245, 245, 245);
    private static final Color BTN_COLOR  = new Color(70, 130, 180);
    private static final Color TXT_COLOR  = Color.WHITE;
    private static final Font  TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font  BTN_FONT   = new Font("SansSerif", Font.PLAIN, 14);
    private static final int   BTN_W      = 180;
    private static final int   BTN_H      = 40;

    StudentFrame(String user) {
        super("Genius Tuition Centre ‒ Student Panel");

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(BG_COLOR);
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setContentPane(root);

        JLabel lblTitle = new JLabel("Welcome, Student!", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new GridBagLayout());
        pnlButtons.setBackground(BG_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets    = new Insets(10, 10, 10, 10);
        gc.fill      = GridBagConstraints.HORIZONTAL;
        gc.weightx   = 1;

        String[] labels = {
            "View Schedule",
            "Enroll/Request Change",
            "View Requests",
            "Payment Status",
            "Profile"
        };
        Runnable[] actions = {
            () -> new ViewScheduleDialog(this, user),
            () -> new RequestSubjectDialog(this, user),
            () -> new ViewRequestsDialog(this, user),
            () -> new PaymentStatusDialog(this, user),
            () -> new ProfileDialog(this, user, "Student")
        };

        for (int i = 0; i < labels.length; i++) {

            if (i < 4) {
                gc.gridx     = i % 2;
                gc.gridy     = i / 2;
                gc.gridwidth = 1;
                gc.anchor    = GridBagConstraints.CENTER;
                gc.fill      = GridBagConstraints.HORIZONTAL;
            } else {
                gc.gridx     = 0;
                gc.gridy     = 2;
                gc.gridwidth = 2;
                gc.anchor    = GridBagConstraints.CENTER;
                gc.fill      = GridBagConstraints.NONE;
            }

            JButton btn = makeButton(labels[i], actions[i]);

            btn.setPreferredSize(new Dimension(BTN_W, BTN_H));
            pnlButtons.add(btn, gc);
        }
        root.add(pnlButtons, BorderLayout.CENTER);

        JButton btnLogout = makeButton("Logout", () -> {
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new);
        });
        btnLogout.setPreferredSize(new Dimension(BTN_W, BTN_H));
        JPanel pnlSouth = new JPanel();
        pnlSouth.setBackground(BG_COLOR);
        pnlSouth.add(btnLogout);
        root.add(pnlSouth, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(BTN_FONT);
        btn.setBackground(BTN_COLOR);
        btn.setForeground(TXT_COLOR);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> action.run());
        return btn;
    }
}

//Tutor Management(admin/receptionist)
class TutorManagementDialog extends JDialog {
    private JTable table;

    public TutorManagementDialog(JFrame owner) {
        super(owner, "Tutor Management", true);

        List<String[]> raw = DataManager.loadAll("tutors.txt");
        String[] cols = { "ID", "Name", "Level", "Subject", "Phone" };
        String[][] rows = new String[raw.size()][cols.length];
        for (int i = 0; i < raw.size(); i++) {
    String[] r = raw.get(i);
    int n = r.length;
    rows[i][0] = r[0];  // ID
    rows[i][1] = String.join(" ", Arrays.copyOfRange(r,1,n-3)); // Name
    rows[i][2] = r[n-3]; // Level
    rows[i][3] = r[n-2].replace(";", ", ");
    rows[i][4] = r[n-1]; // Phone
}
        table = new JTable(rows, cols);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add"),
                editBtn = new JButton("Edit"),
                delBtn = new JButton("Delete"),
                backBtn = new JButton("Back");
        btnPanel.add(addBtn); btnPanel.add(editBtn);
        btnPanel.add(delBtn); btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Please select a tutor to edit",
                                              "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openForm(table.getValueAt(sel,0).toString());
        });
        delBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel<0) {
                JOptionPane.showMessageDialog(this, "Please select a tutor to delete",
                                              "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = table.getValueAt(sel,0).toString();
            if (JOptionPane.showConfirmDialog(this,
                    "Delete tutor "+id+"?", "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
                DataManager.delete("tutors.txt", 0, id);
                DataManager.delete("users.txt", 0, id);
                dispose();
                new TutorManagementDialog(owner);
            }
        });
        backBtn.addActionListener(e -> dispose());

        setSize(700,400);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

private void openForm(String id) {
    boolean isNew = (id == null);
    
    JTextField tfUsername = new JTextField(15);
    JPasswordField pfPassword = new JPasswordField(15);
    JTextField tfId      = new JTextField(isNew ? genId() : id, 15);
    JTextField tfName    = new JTextField(20);
    JTextField tfIC      = new JTextField(15);
    JTextField tfEmail   = new JTextField(20);
    JTextField tfAddress = new JTextField(30);
    JTextField tfPhone   = new JTextField(15);

    JComboBox<String> cbLevel = new JComboBox<>(new String[]{ "F1-F3", "F4-F5" });

    List<JCheckBox> subjChecks = new ArrayList<>();
    JPanel subjectPanel = new JPanel();
    subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));
    JScrollPane subjectScroll = new JScrollPane(subjectPanel);
    subjectScroll.setPreferredSize(new Dimension(150, 120));

    ActionListener rebuildSubjects = e -> {
        String lvlGroup = (String) cbLevel.getSelectedItem();
        subjectPanel.removeAll();
        subjChecks.clear();

        // base subjects
        String[] base = { "BM", "BI", "Math", "Science", "BC", "History" };
        for (String s : base) {
            JCheckBox cb = new JCheckBox(s);
            subjChecks.add(cb);
            subjectPanel.add(cb);
        }

        // advanced for F4-F5
        if ("F4-F5".equals(lvlGroup)) {
            String[] advanced = { "Physics", "Chemistry", "Biology", "Accounting", "AddMath" };
            for (String s : advanced) {
                JCheckBox cb = new JCheckBox(s);
                subjChecks.add(cb);
                subjectPanel.add(cb);
            }
        }

        subjectPanel.revalidate();
        subjectPanel.repaint();
    };
    cbLevel.addActionListener(rebuildSubjects);
    cbLevel.setSelectedIndex(0);
    rebuildSubjects.actionPerformed(null);

    if (!isNew) {
        String oldSubs = "";
        for (String[] r : DataManager.loadAll("tutors.txt")) {
            if (r[0].equals(id)) {
                tfName.setText(String.join(" ", Arrays.copyOfRange(r, 1, r.length - 3)));
                String storedLevel = r[r.length - 3];
                cbLevel.setSelectedItem(storedLevel);
                oldSubs = r[r.length - 2];
                tfPhone.setText(r[r.length - 1]);
                break;
            }
        }

        rebuildSubjects.actionPerformed(null);
        // select previous subjects
        Set<String> oldSet = new HashSet<>(Arrays.asList(oldSubs.split(";")));
        for (JCheckBox cb : subjChecks) {
            if (oldSet.contains(cb.getText())) cb.setSelected(true);
        }
        for (String[] u : DataManager.loadAll("users.txt")) {
            if (u[0].equals(id)) {
                tfIC.setText(u.length > 5 ? u[5] : "");
                tfEmail.setText(u.length > 6 ? u[6] : "");
                tfAddress.setText(u.length > 8 ? u[8] : "");
                break;
            }
        }
    }

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6,12,6,12);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    int y = 0;

    if (isNew) {
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Username:"), gbc);
        gbc.gridx=1;               form.add(tfUsername, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Password:"), gbc);
        gbc.gridx=1;               form.add(pfPassword, gbc); y++;
    }

    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("ID:"), gbc);
    gbc.gridx=1;               form.add(tfId, gbc); y++;
    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Name:"), gbc);
    gbc.gridx=1;               form.add(tfName, gbc); y++;
    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("IC:"), gbc);
    gbc.gridx=1;               form.add(tfIC, gbc); y++;
    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Email:"), gbc);
    gbc.gridx=1;               form.add(tfEmail, gbc); y++;
    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Address:"), gbc);
    gbc.gridx=1;               form.add(tfAddress, gbc); y++;
    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Phone:"), gbc);
    gbc.gridx=1;               form.add(tfPhone, gbc); y++;
    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Level Group:"), gbc);
    gbc.gridx=1;               form.add(cbLevel, gbc); y++;
    gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Subjects:"), gbc);
    gbc.gridx=1;               form.add(subjectScroll, gbc); y++;

    int res = JOptionPane.showConfirmDialog(
        this, form,
        isNew ? "Add Tutor" : "Edit Tutor",
        JOptionPane.OK_CANCEL_OPTION
    );
    if (res != JOptionPane.OK_OPTION) return;

    List<String> chosen = new ArrayList<>();
    for (JCheckBox cb : subjChecks) {
        if (cb.isSelected()) chosen.add(cb.getText());
    }
    String subjRec = String.join(";", chosen);

    String[] tutorRec = new String[]{
        tfId.getText().trim(),
        tfName.getText().trim(),
        cbLevel.getSelectedItem().toString(),
        subjRec,
        tfPhone.getText().trim()
    };

    if (isNew) {
        DataManager.append("tutors.txt", tutorRec);
        DataManager.append("users.txt", new String[]{
            tfId.getText().trim(),                        
            new String(pfPassword.getPassword()).trim(), 
            "Tutor",                          
            tfId.getText().trim(),
            tfName.getText().trim(),
            tfIC.getText().trim(),
            tfEmail.getText().trim(),
            tfPhone.getText().trim(),
            tfAddress.getText().trim()
        });
    } else {
        for (String[] u : DataManager.loadAll("users.txt")) {
            if (u[0].equals(id)) {
                String[] updatedUser = new String[]{
                    u[0],
                    u.length > 1 ? u[1] : "",
                    u.length > 2 ? u[2] : "Tutor",
                    u.length > 3 ? u[3] : tfId.getText().trim(),
                    tfName.getText().trim(),
                    tfIC.getText().trim(),
                    tfEmail.getText().trim(),
                    tfPhone.getText().trim(),
                    tfAddress.getText().trim()
                };
                DataManager.update("users.txt", 0, id, updatedUser);
                break;
            }
        }
        DataManager.update("tutors.txt", 0, id, tutorRec);
    }

    dispose();
    new TutorManagementDialog((JFrame) getOwner());
}


    private String genId() {
        return String.format("T%03d",
            DataManager.loadAll("tutors.txt").stream()
                .mapToInt(r -> {
                    try { return Integer.parseInt(r[0].substring(1)); }
                    catch (Exception ex) { return 0; }
                }).max().orElse(0) + 1
        );
    }
}


// Receptionist Management(admin/receptionist)
class ReceptionistManagementDialog extends JDialog {
    private final DefaultTableModel model;
    private final JTable table;

    ReceptionistManagementDialog(JFrame owner) {
        super(owner, "Receptionist Management", true);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{ "ID", "Name", "Phone", "IC", "Email", "Address" }, 0) {
        @Override public boolean isCellEditable(int row, int column) {
        return false;
    }
        };
        table = new JTable(model);
        // only allow single‑row selection
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        JPanel btnPanel = new JPanel();
        JButton addBtn  = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn  = new JButton("Delete");
        JButton backBtn = new JButton("Back");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));

        editBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this,
                    "Please select a row to edit", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = model.getValueAt(sel, 0).toString();
            openForm(id);
            dispose();
            new ReceptionistManagementDialog((JFrame) getOwner());
        });

        delBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this,
                    "Please select a row to delete", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = model.getValueAt(sel, 0).toString();

            if (JOptionPane.showConfirmDialog(
                    this,
                    "Delete \"" + id + "\"?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
                ) != JOptionPane.YES_OPTION) return;

            DataManager.delete("receptionists.txt", 0, id);
            DataManager.delete("users.txt",       3, id);

            loadData();
            model.fireTableDataChanged();
            table.clearSelection();
            table.revalidate();
            table.repaint();
        });

        backBtn.addActionListener(e -> dispose());

        setSize(500, 300);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void loadData() {
    model.setRowCount(0);
    List<String[]> recs = DataManager.loadAll("receptionists.txt");
    List<String[]> users = DataManager.loadAll("users.txt");

    for (String[] r : recs) {
        String id = r[0];
        String name = r.length > 1 ? r[1] : "";
        String phone = r.length > 2 ? r[2] : "";
        String ic = "";
        String email = "";
        String address = "";

        for (String[] u : users) {
            if (u.length >= 9 && u[3].equals(id)) {
                ic = u.length > 5 ? u[5] : "";
                email = u.length > 6 ? u[6] : "";
                address = u.length > 8 ? u[8] : "";
                break;
            }
        }

        model.addRow(new Object[]{ id, name, phone, ic, email, address });
    }
}
private void openForm(String id) {
    boolean isNew = (id == null);

    JTextField tfUsername = new JTextField(12);
    JPasswordField pfPassword = new JPasswordField(12);
    JTextField tfId    = new JTextField(isNew ? genId() : id, 10);
    JTextField tfName  = new JTextField(20);
    JTextField tfPhone = new JTextField(15);
    JTextField tfIC    = new JTextField(15);
    JTextField tfEmail = new JTextField(20);
    JTextField tfAddress = new JTextField(30);

    if (!isNew) {
        for (String[] u : DataManager.loadAll("users.txt")) {
            if (u.length >= 9 && u[3].equals(id)) {
                tfUsername.setText(u[0]);
                pfPassword.setText(u[1]);
                tfIC.setText(u[5]);
                tfEmail.setText(u[6]);
                tfAddress.setText(u[8]);
                break;
            }
        }

        for (String[] r : DataManager.loadAll("receptionists.txt")) {
            if (r[0].equals(id)) {
                tfName.setText(r[1]);
                tfPhone.setText(r[2]);
                break;
            }
        }
    }

    JPanel form = new JPanel(new GridLayout(8, 2, 5, 5));
    form.add(new JLabel("Username:")); form.add(tfUsername);
    form.add(new JLabel("Password:")); form.add(pfPassword);
    form.add(new JLabel("ID:"));       form.add(tfId);
    form.add(new JLabel("Name:"));     form.add(tfName);
    form.add(new JLabel("IC:"));       form.add(tfIC);
    form.add(new JLabel("Email:"));    form.add(tfEmail);
    form.add(new JLabel("Address:"));  form.add(tfAddress);
    form.add(new JLabel("Phone:"));    form.add(tfPhone);

    int res = JOptionPane.showConfirmDialog(
        this, form,
        isNew ? "Add Receptionist" : "Edit Receptionist",
        JOptionPane.OK_CANCEL_OPTION
    );
    if (res != JOptionPane.OK_OPTION) return;

    String[] rec = {
        tfId.getText().trim(),
        tfName.getText().trim(),
        tfPhone.getText().trim()
    };

    String[] userRec = {
        tfUsername.getText().trim(),
        new String(pfPassword.getPassword()).trim(),
        "Receptionist",
        tfId.getText().trim(),
        tfName.getText().trim(),
        tfIC.getText().trim(),
        tfEmail.getText().trim(),
        tfPhone.getText().trim(),
        tfAddress.getText().trim()
    };

    if (isNew) {
        DataManager.append("receptionists.txt", rec);
        DataManager.append("users.txt",        userRec);
        model.addRow(new Object[]{ rec[0], rec[1], rec[2] });
    } else {
        DataManager.update("receptionists.txt", 0, id, rec);
        DataManager.update("users.txt",        3, id, userRec);
    }
}

    private String genId() {
        int max = 0;
        for (String[] r : DataManager.loadAll("receptionists.txt")) {
            try {
                int n = Integer.parseInt(r[0].substring(1));
                max = Math.max(max, n);
            } catch (Exception ignored) {}
        }
        return String.format("R%03d", max + 1);
    }
}

// Student Management(admin/receptionist)
class StudentManagementDialog extends JDialog {
    private JTable table;

    public StudentManagementDialog(JFrame owner) {
        super(owner, "Student Management", true);
        setLayout(new BorderLayout());

        List<String[]> raw = DataManager.loadAll("students.txt");
        String[] cols = {
            "ID","Name","IC","Email","Phone",
            "Address","Level","Subjects","Enrollment Month"
        };
        String[][] data = new String[raw.size()][cols.length];
        for (int i = 0; i < raw.size(); i++) {
            String[] r = raw.get(i);

            String id      = r[0];
            String name    = r[1];
            String ic      = r[2];
            String email   = r[3];
            String phone   = r[4];
            String address = r[5];
            String level   = r[6];

            String[] subjArr = Arrays.copyOfRange(r, 7, r.length - 1);
            String subj = String.join(", ", subjArr);

            String month = r[r.length - 1];

            data[i] = new String[]{
                id, name, ic, email, phone,
                address, level, subj, month
            };
        }

        table = new JTable(data, cols);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton add = new JButton("Add"),
                edit = new JButton("Edit"),
                del = new JButton("Delete"),
                close = new JButton("Back");
        btns.add(add); btns.add(edit); btns.add(del); btns.add(close);
        add(btns, BorderLayout.SOUTH);

        add.addActionListener(e -> openForm(null));

        edit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this,
                    "Please select a student to edit",
                    "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = table.getValueAt(r, 0).toString();
            openForm(id);
        });

        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this,
                    "Please select a student to delete",
                    "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = table.getValueAt(r, 0).toString();
            if (JOptionPane.showConfirmDialog(this,
                    "Delete student \"" + id + "\"?",
                    "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
                DataManager.delete("students.txt", 0, id);
                DataManager.delete("users.txt",    0, id);
                dispose();
                new StudentManagementDialog(owner);
            }
        });

        close.addActionListener(e -> dispose());

        setSize(800, 400);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void openForm(String id) {
        boolean isNew = (id == null);

        JTextField tfUsername = new JTextField(12);
        JPasswordField pfPassword = new JPasswordField(12);

        JTextField tfId    = new JTextField(isNew ? genId() : id, 8);
        JTextField tfName  = new JTextField(15);
        JTextField tfIC    = new JTextField(15);
        JTextField tfEmail = new JTextField(15);
        JTextField tfPhone = new JTextField(15);
        JTextField tfAddr  = new JTextField(20);

        JComboBox<String> cbLevel = new JComboBox<>(new String[]{ "F1","F2","F3","F4","F5" });

        JTextField tfMonth = new JTextField(8);

        JPanel subjPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        List<JCheckBox> checks = new ArrayList<>();

        ActionListener rebuild = ev -> {
            subjPanel.removeAll();
            checks.clear();

           String[] base = { "BM","BI","Math","Science","BC","History" };
            for (String s : base) {
                JCheckBox cb = new JCheckBox(s);
                cb.addItemListener(il -> {
                    long selected = checks.stream().filter(JCheckBox::isSelected).count();
                    if (selected > 3) {
                        if (cb.isSelected()) cb.setSelected(false);
                        JOptionPane.showMessageDialog(this,
                            "You can select up to 3 subjects only.",
                            "Selection Limit", JOptionPane.WARNING_MESSAGE);
                    }
                });
                checks.add(cb);
                subjPanel.add(cb);
            }

            String lvl = (String) cbLevel.getSelectedItem();
            if ("F4".equals(lvl) || "F5".equals(lvl)) {
                String[] extras = { "Physics","Chemistry","Biology","Account" };
                for (String s : extras) {
                    JCheckBox cb = new JCheckBox(s);
                    cb.addItemListener(il -> {
                        long selected = checks.stream().filter(JCheckBox::isSelected).count();
                        if (selected > 3) {
                            if (cb.isSelected()) cb.setSelected(false);
                            JOptionPane.showMessageDialog(this,
                                "You can select up to 3 subjects only.",
                                "Selection Limit", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                    checks.add(cb);
                    subjPanel.add(cb);
                }
            }

            subjPanel.revalidate();
            subjPanel.repaint();
        };

        cbLevel.addActionListener(rebuild);
        rebuild.actionPerformed(null);
        if (isNew) {
            cbLevel.setSelectedItem("F4");
            rebuild.actionPerformed(null);
        }

        if (!isNew) {
            List<String[]> all = DataManager.loadAll("students.txt");
            for (String[] r : all) {
                if (r[0].equals(id)) {
                    tfName.setText(r[1]);
                    tfIC.setText(r[2]);
                    tfEmail.setText(r[3]);
                    tfPhone.setText(r[4]);
                    tfAddr.setText(r[5]);

                    cbLevel.setSelectedItem(r[6]);
                    rebuild.actionPerformed(null);

                    List<String> chosen = Arrays.asList(
                        Arrays.copyOfRange(r, 7, r.length - 1)
                    );
                    for (JCheckBox cb : checks) {
                        if (chosen.contains(cb.getText())) {
                            cb.setSelected(true);
                        }
                    }

                    tfMonth.setText(r[r.length - 1]);
                    break;
                }
            }
        }

        int rows = isNew ? 11 : 9;
        JPanel form = new JPanel(new GridLayout(rows, 2, 5, 5));
        if (isNew) {
            form.add(new JLabel("Username:")); form.add(tfUsername);
            form.add(new JLabel("Password:")); form.add(pfPassword);
        }
        form.add(new JLabel("ID:"));       form.add(tfId);
        form.add(new JLabel("Name:"));     form.add(tfName);
        form.add(new JLabel("IC:"));       form.add(tfIC);
        form.add(new JLabel("Email:"));    form.add(tfEmail);
        form.add(new JLabel("Phone:"));    form.add(tfPhone);
        form.add(new JLabel("Address:"));  form.add(tfAddr);
        form.add(new JLabel("Level:"));    form.add(cbLevel);
        form.add(new JLabel("Subjects:")); form.add(subjPanel);
        form.add(new JLabel("Enrollment Month(yyyy-mm):"));    form.add(tfMonth);

        int res = JOptionPane.showConfirmDialog(
            this, form,
            isNew ? "Add Student" : "Edit Student",
            JOptionPane.OK_CANCEL_OPTION
        );
        if (res == JOptionPane.OK_OPTION) {
            List<String> selSubs = checks.stream()
                .filter(JCheckBox::isSelected)
                .map(JCheckBox::getText)
                .collect(Collectors.toList());

            List<String> rec = new ArrayList<>(Arrays.asList(
                tfId.getText().trim(),
                tfName.getText().trim(),
                tfIC.getText().trim(),
                tfEmail.getText().trim(),
                tfPhone.getText().trim(),
                tfAddr.getText().trim(),
                cbLevel.getSelectedItem().toString()
            ));
            rec.addAll(selSubs);
            rec.add(tfMonth.getText().trim());

            String[] record = rec.toArray(new String[0]);

            if (isNew) {
                DataManager.append("students.txt", record);

                DataManager.append("users.txt", new String[]{
                    tfId.getText().trim(),                     // username
                    new String(pfPassword.getPassword()).trim(),// password
                    "Student",                                 // role
                    tfId.getText().trim(),                     // id
                    tfName.getText().trim(),                   // name
                    tfIC.getText().trim(),                     // IC
                    tfEmail.getText().trim(),                  // email
                    tfPhone.getText().trim(),                  // phone
                    tfAddr.getText().trim()                    // address
                });
            } else {
                // update student.txt
                DataManager.update("students.txt", 0, id, record);

                List<String[]> users = DataManager.loadAll("users.txt");
                for (String[] u : users) {
                    if (u.length >= 4 && u[3].equals(id)) {
                        String pass = u.length > 1 ? u[1] : "";
                        String[] updUser = new String[]{
                            u[0],                        // username
                            pass,                        // password preserved
                            "Student",                   // role
                            tfId.getText().trim(),       // id
                            tfName.getText().trim(),     // name
                            tfIC.getText().trim(),       // IC
                            tfEmail.getText().trim(),    // email
                            tfPhone.getText().trim(),    // phone
                            tfAddr.getText().trim()      // address
                        };
                        DataManager.update("users.txt", 0, u[0], updUser);
                        break;
                    }
                }
            }

            dispose();
            new StudentManagementDialog((JFrame) getOwner());
        }
    }

    private String genId() {
        List<String[]> all = DataManager.loadAll("students.txt");
        int max = all.stream().mapToInt(r -> {
            try {return Integer.parseInt(r[0].substring(1));}
            catch (Exception e) {return 0;}
        }).max().orElse(0);
        return String.format("S%03d", max+1);
    }
}



// Add Payment (admin/receptionist)
class AddPaymentDialog extends JDialog {
    private static final String[] MONTHS = {
        "January","February","March","April",
        "May","June","July","August",
        "September","October","November","December"
    };

    public AddPaymentDialog(JFrame owner, String studentIdDefault) {
        super(owner, "Add Payment", true);
        setLayout(new BorderLayout(5,5));

        List<String[]> studs = DataManager.loadAll("students.txt");
        String[] stuEntries = studs.stream()
            .map(r -> r[0] + " - " + r[1])
            .toArray(String[]::new);
        JComboBox<String> cbStudent = new JComboBox<>(stuEntries);
        if (studentIdDefault != null) {
            for (int i = 0; i < studs.size(); i++) {
                if (studs.get(i)[0].equals(studentIdDefault)) {
                    cbStudent.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Billing month/year selectors
        JComboBox<String> cbMonth = new JComboBox<>(MONTHS);
        String currMonthName = java.time.LocalDate.now()
            .getMonth()
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        cbMonth.setSelectedItem(currMonthName);

        int currentYear = java.time.LocalDate.now().getYear();
        JComboBox<String> cbYear = new JComboBox<>();
        cbYear.addItem(String.valueOf(currentYear - 1));
        cbYear.addItem(String.valueOf(currentYear));
        cbYear.addItem(String.valueOf(currentYear + 1));
        cbYear.setSelectedItem(String.valueOf(currentYear));

        JComboBox<String> cbSub = new JComboBox<>();
        JTextField tfAmt = new JTextField(10);
        tfAmt.setEditable(false);

        Runnable reloadSubjects = () -> {
            cbSub.removeAllItems();
            String stuSel = ((String)cbStudent.getSelectedItem()).split(" - ")[0];
            String chosenMonth = (String) cbMonth.getSelectedItem();
            String chosenYear = (String) cbYear.getSelectedItem();
            String billingPeriod = chosenYear + "-" + String.format("%02d",
                java.time.Month.valueOf(chosenMonth.toUpperCase(Locale.ENGLISH)).getValue());

            String[] rec = studs.stream()
                .filter(r -> r[0].equals(stuSel))
                .findFirst().orElse(new String[0]);
            if (rec.length < 8) return;
            List<String> allSubs = Arrays.asList(
                Arrays.copyOfRange(rec, 7, rec.length - 1)
            );

            // load existing payments and skip subjects already paid for this billing month
            List<String[]> pays = DataManager.loadAll("payments.txt");
            for (String subj : allSubs) {
                boolean already = pays.stream().anyMatch(p ->
                    p[1].equals(stuSel) &&
                    p[2].equals(subj) &&
                    p[4].equals(cbMonth.getSelectedItem()) &&
                    p[5].startsWith(chosenYear) // assume timestamp starts with yyyy-
                );
                if (!already) cbSub.addItem(subj);
            }
            if (cbSub.getItemCount() > 0) {
                cbSub.setSelectedIndex(0);
                updateAmt(cbSub, tfAmt);
            } else {
                tfAmt.setText("");
            }
        };

        // update amount based on selected subject
        cbSub.addActionListener(e -> updateAmt(cbSub, tfAmt));
        cbStudent.addActionListener(e -> reloadSubjects.run());
        cbMonth.addActionListener(e -> reloadSubjects.run());
        cbYear.addActionListener(e -> reloadSubjects.run());
        reloadSubjects.run();

        String nextId = "P" + String.format("%03d",
            DataManager.loadAll("payments.txt").size() + 1);
        JTextField tfId = new JTextField(nextId, 8);
        tfId.setEditable(false);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String now = java.time.LocalDateTime.now().format(dtf);
        JTextField tfDT = new JTextField(now, 16);
        tfDT.setEditable(false);

        JPanel form = new JPanel(new GridLayout(8,2,5,5));
        form.add(new JLabel("Payment ID:")); form.add(tfId);
        form.add(new JLabel("Student:"));    form.add(cbStudent);
        form.add(new JLabel("Billing Month:")); form.add(cbMonth);
        form.add(new JLabel("Billing Year:"));  form.add(cbYear);
        form.add(new JLabel("Subject:"));    form.add(cbSub);
        form.add(new JLabel("Amount:"));     form.add(tfAmt);
        form.add(new JLabel("Date/Time:"));  form.add(tfDT);
        add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton ok = new JButton("OK"), cancel = new JButton("Cancel");
        btns.add(ok); btns.add(cancel);
        add(btns, BorderLayout.SOUTH);

        ok.addActionListener(evt -> {
            String pid   = tfId.getText().trim();
            String sid   = ((String)cbStudent.getSelectedItem()).split(" - ")[0];
            String name  = ((String)cbStudent.getSelectedItem()).split(" - ")[1];
            String subj  = (String) cbSub.getSelectedItem();
            String amt   = tfAmt.getText().trim();
            String mon   = (String)cbMonth.getSelectedItem();
            String ts    = tfDT.getText().trim();

            DataManager.append("payments.txt", new String[]{
            pid, sid, subj, amt, mon, ts
            });

            String receipt =
            "Receipt\n" +
            "-------\n" +
            "Payment ID: " + pid + "\n" +
            "Student:    " + name + " (" + sid + ")\n" +
            "Subject:    " + subj + "\n" +
            "Month:      " + mon + "\n" +
            "Amount:     RM " + String.format("%.2f", Double.parseDouble(amt)) + "\n" +
            "Date/Time:  " + ts + "\n";

            String safeName = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String datePart = ts.length() >= 16 ? ts.substring(0, 16).replace(":", "-").replace(" ", "_") : LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String baseFileName = safeName + "_" + pid + "_" + datePart + ".txt";
            File receiptFile = new File(baseFileName);
            int attempt = 1;
            while (receiptFile.exists()) {
            receiptFile = new File(safeName + "_" + pid + "_" + datePart + "_" + attempt + ".txt");
            attempt++;
            if (attempt > 10) break;
            }
            
            try (PrintWriter out = new PrintWriter(new FileWriter(receiptFile))) {
            out.print(receipt);
            } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Payment recorded but failed to write receipt: " + ex.getMessage(),
                "IO Error", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
            }
            
            JOptionPane.showMessageDialog(this, receipt,
            "Payment Recorded", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            });

        cancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }

    private void updateAmt(JComboBox<String> cbSub, JTextField tfAmt) {
        String subject = (String) cbSub.getSelectedItem();
        if (subject == null) {
            tfAmt.setText("");
            return;
        }
        List<String[]> charges = DataManager.loadAll("charges.txt");
        for (String[] r : charges) {
            if (r.length >= 2 && r[0].equalsIgnoreCase(subject)) {
                tfAmt.setText(r[1]);
                return;
            }
        }
        tfAmt.setText("");
    }
}


                
// Reports dialog (admin/receptionist)
class ReportDialog extends JDialog {
    private static final Set<String> EXTRA = new HashSet<>(
        Arrays.asList("Physics", "Chemistry", "Biology", "Account", "Addmath")
    );
    private static final double BASE = 50.0;
    private static final double ADV  = 90.0;

    ReportDialog(JFrame owner) {
        super(owner, "Monthly Income Report", true);
        setLayout(new BorderLayout(10, 10));

        List<String[]> pays = DataManager.loadAll("payments.txt");

        Set<String> yearSet = new TreeSet<>();
        Set<String> monthSet = new TreeSet<>();

        Map<String, String[]> sinfo = new HashMap<>();
        DataManager.loadAll("students.txt").forEach(r ->
            sinfo.put(r[0], new String[]{ r[1], r[6] })
        );
        
        TreeMap<String, Double> agg = new TreeMap<>();
        Map<String, Double> expectedPerKey = new HashMap<>(); 
        Map<String, List<String>> payIdsPerKey = new HashMap<>();
        Map<String, String> latestTimestampPerKey = new HashMap<>(); 

        DefaultTableModel detM = new DefaultTableModel(
            new String[]{"PayID", "Timestamp", "Year", "Month", "Subject", "StuID", "Name", "Level", "Amount"}, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (String[] p : pays) {
            if (p.length < 6) continue;
            String payId = p[0];
            String sid = p[1];
            String subj = p[2];
            double amt = 0;
            try { amt = Double.parseDouble(p[3]); } catch (Exception ignored) {}
            String billingMonthRaw = p[4];
            String ts = p[5];

            String billingYear = "";
            if (ts.length() >= 10) {
                try {
                    LocalDate d = LocalDate.parse(ts.substring(0, 10));
                    billingYear = String.valueOf(d.getYear());
                } catch (Exception ignored) {}
            }

            String billingMonth = billingMonthRaw;
            try {

                int m = Integer.parseInt(billingMonthRaw);
                billingMonth = LocalDate.of(2000, m, 1)
                    .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            } catch (Exception ignored) {
                if (billingMonth.length() > 0) {
                    billingMonth = billingMonth.substring(0,1).toUpperCase() + billingMonth.substring(1);
                }
            }

            yearSet.add(billingYear);
            monthSet.add(billingMonth);

            String key = billingYear + "|" + billingMonth + "|" + subj + "|" + sid;

            agg.put(key, agg.getOrDefault(key, 0.0) + amt);

            double exp = EXTRA.contains(subj) ? ADV : BASE;
            expectedPerKey.put(key, exp);

            payIdsPerKey.computeIfAbsent(key, k -> new ArrayList<>()).add(payId);

            String prevTs = latestTimestampPerKey.get(key);
            if (prevTs == null || ts.compareTo(prevTs) > 0) {
                latestTimestampPerKey.put(key, ts);
            }

            String[] info = sinfo.getOrDefault(sid, new String[]{"<Unknown>", ""});
            String name = info[0];
            String level = info[1];
            detM.addRow(new Object[]{
                payId,
                ts,
                billingYear,
                billingMonth,
                subj,
                sid,
                name,
                level,
                String.format("%.2f", amt)
            });
        }

        // Summary model
        DefaultTableModel sumM = new DefaultTableModel(
            new String[]{
                "PayIDs", "Year", "Month", "Subject", "StuID", "Name", "Level", "Amount Paid", "Expected", "Status"
            }, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Map.Entry<String, Double> e : agg.entrySet()) {
            String key = e.getKey();
            String[] parts = key.split("\\|");
            if (parts.length < 4) continue;
            String billingYear = parts[0];
            String billingMonth = parts[1];
            String subj = parts[2];
            String sid = parts[3];
            double paid = e.getValue();
            double exp = expectedPerKey.getOrDefault(key, EXTRA.contains(subj) ? ADV : BASE);
            String status = paid + 1e-6 >= exp ? "Paid" : "Unpaid";
            String[] info = sinfo.getOrDefault(sid, new String[]{"<Unknown>", ""});
            String name = info[0];
            String level = info[1];
            String payIds = String.join(", ", payIdsPerKey.getOrDefault(key, List.of()));

            sumM.addRow(new Object[]{
                payIds,
                billingYear,
                billingMonth,
                subj,
                sid,
                name,
                level,
                String.format("%.2f", paid),
                String.format("%.2f", exp),
                status
            });
        }

        JComboBox<String> cbYear = new JComboBox<>();
        cbYear.addItem("All");
        yearSet.forEach(cbYear::addItem);

        JComboBox<String> cbMonth = new JComboBox<>();
        cbMonth.addItem("All");
        
        List<String> monthsSorted = new ArrayList<>(monthSet);
        List<String> calendarOrder = Arrays.stream(java.time.Month.values())
            .map(m -> m.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
            .collect(Collectors.toList());
        monthsSorted.sort(Comparator.comparingInt(m -> {
            int idx = calendarOrder.indexOf(m);
            return idx >= 0 ? idx : Integer.MAX_VALUE;
        }));
        monthsSorted.forEach(cbMonth::addItem);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.add(new JLabel("Year:"));  filterPanel.add(cbYear);
        filterPanel.add(new JLabel("Month:")); filterPanel.add(cbMonth);
        add(filterPanel, BorderLayout.NORTH);

        JTable sumT = new JTable(sumM);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(sumM);
        sumT.setRowSorter(sorter);
        JTable detT = new JTable(detM);

        JLabel lblTotal = new JLabel("Total Income: RM 0.00", SwingConstants.RIGHT);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));

        // filter
        ActionListener filterListener = evt -> {
            String selYear = (String) cbYear.getSelectedItem();
            String selMonth = (String) cbMonth.getSelectedItem();

            List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

            if (!"All".equals(selYear)) {
                filters.add(RowFilter.regexFilter("^" + Pattern.quote(selYear) + "$", 1)); // Year column
            }
            if (!"All".equals(selMonth)) {
                filters.add(RowFilter.regexFilter("^" + Pattern.quote(selMonth) + "$", 2)); // Month column
            }
            if (filters.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }

            double tot = 0;
            for (int i = 0; i < sumT.getRowCount(); i++) {
                int modelIdx = sumT.convertRowIndexToModel(i);
                try {
                    tot += Double.parseDouble(sumM.getValueAt(modelIdx, 7).toString());
                } catch (Exception ignored) {}
            }
            lblTotal.setText(String.format("Total Income: RM %.2f", tot));
        };
        cbYear.addActionListener(filterListener);
        cbMonth.addActionListener(filterListener);
        filterListener.actionPerformed(null); // initial

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Summary", new JScrollPane(sumT));
        tabs.addTab("Detail", new JScrollPane(detT));
        add(tabs, BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        JPanel south = new JPanel(new BorderLayout());
        south.add(lblTotal, BorderLayout.WEST);
        south.add(btnClose, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        setSize(1000, 600);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}


//Approve Requests(admin/receptionist)
class ApproveRequestsDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;

    public ApproveRequestsDialog(JFrame owner) {
        super(owner, "Approve Subject Request", true);
        setLayout(new BorderLayout(10,10));

        List<String[]> all = DataManager.loadAll("requests.txt");
        String[] cols = {"ReqID","StudentID","Type","OldSubj","NewSubj","Status","Date"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] r : all) model.addRow(r);

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton btnApprove = new JButton("Approve");
        JButton btnReject  = new JButton("Reject");
        JButton btnDelete  = new JButton("Delete");
        JButton btnBack    = new JButton("Back");
        btns.add(btnApprove);
        btns.add(btnReject);
        btns.add(btnDelete);
        btns.add(btnBack);
        add(btns, BorderLayout.SOUTH);

        btnApprove.addActionListener(e -> changeStatus("Approved"));
        btnReject .addActionListener(e -> changeStatus("Rejected"));
        btnDelete .addActionListener(e -> deleteRequest());
        btnBack   .addActionListener(e -> dispose());

        setSize(800, 400);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void changeStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a request to " + status.toLowerCase(),
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reqId     = model.getValueAt(row, 0).toString();
        String studentId = model.getValueAt(row, 1).toString();
        String type      = model.getValueAt(row, 2).toString();
        String oldSubj   = model.getValueAt(row, 3).toString();
        String newSubj   = model.getValueAt(row, 4).toString();
        String current   = model.getValueAt(row, 5).toString();

        if (!"PENDING".equalsIgnoreCase(current)) {
            JOptionPane.showMessageDialog(this,
                "Already " + current.toLowerCase() + " or invalid!",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // If approving a NEW subject, enforce max 3
        if ("Approved".equalsIgnoreCase(status) && "New".equalsIgnoreCase(type)) {
            for (String[] stu : DataManager.loadAll("students.txt")) {
                if (!stu[0].equals(studentId)) continue;
                // count existing subjects (columns 7..length-2)
                int subjectCount = Math.max(0, stu.length - 1 - 7);
                if (subjectCount >= 3) {
                    JOptionPane.showMessageDialog(this,
                        "Cannot approve new subject: student already has 3 subjects.",
                        "Limit Reached", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                break;
            }
        }

        List<String[]> allReqs = DataManager.loadAll("requests.txt");
        for (String[] r : allReqs) {
            if (r[0].equals(reqId)) {
                r[5] = status.toUpperCase();
                break;
            }
        }
        DataManager.saveAll("requests.txt", allReqs);
        model.setValueAt(status.toUpperCase(), row, 5);

        if ("Approved".equalsIgnoreCase(status)) {
            List<String[]> studs = DataManager.loadAll("students.txt");
            for (int i = 0; i < studs.size(); i++) {
                String[] stu = studs.get(i);
                if (!stu[0].equals(studentId)) continue;

                List<String> fields = new ArrayList<>(Arrays.asList(stu));
                int monthIdx = fields.size() - 1;

                if ("New".equalsIgnoreCase(type)) {
                    fields.add(monthIdx, newSubj);
                } else {
                    for (int j = 7; j < monthIdx; j++) {
                        if (fields.get(j).equals(oldSubj)) {
                            fields.set(j, newSubj);
                            break;
                        }
                    }
                }

                DataManager.update("students.txt", 0, studentId,
                                   fields.toArray(new String[0]));
                break;
            }
        }

        JOptionPane.showMessageDialog(this,
            "Request " + status.toLowerCase() + "!");
    }

    private void deleteRequest() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a request to delete",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String current = table.getValueAt(row, 5).toString();
        if ("PENDING".equalsIgnoreCase(current)) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete a pending request. Please approve or reject it first.",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reqId = model.getValueAt(row, 0).toString();
        DataManager.delete("requests.txt", 0, reqId);
        model.removeRow(row);
        JOptionPane.showMessageDialog(this,
            "Request " + reqId + " deleted.");
    }
}

//Request subject change (Student)
class RequestSubjectDialog extends JDialog {
    public RequestSubjectDialog(JFrame owner, String studentId) {
        super(owner, "Request Subject Change", true);
        setLayout(new BorderLayout(10,10));

        String studentLevel = "";
        List<String> currentSubs = new ArrayList<>();
        String enrollmentMonth = "";
        for (String[] row : DataManager.loadAll("students.txt")) {
            if (row[0].equals(studentId)) {
                studentLevel = row[6];
                for (int i = 7; i < row.length - 1; i++) {
                    currentSubs.add(row[i]);
                }
                enrollmentMonth = row[row.length - 1];
                break;
            }
        }

        List<String> allSubjects = new ArrayList<>(
            Arrays.asList("BM","BI","Math","Science","BC","History")
        );
        if ("F4".equals(studentLevel) || "F5".equals(studentLevel)) {
            allSubjects.addAll(Arrays.asList("Physics","Chemistry","Biology","Account","Addmath"));
        }

        JRadioButton rbEnroll = new JRadioButton("Enroll new subject");
        JRadioButton rbChange = new JRadioButton("Change existing subject");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbEnroll);
        bg.add(rbChange);

        JComboBox<String> cbOld = new JComboBox<>(currentSubs.toArray(new String[0]));
        cbOld.setEnabled(false);

        List<String> potentialNew = new ArrayList<>();
        for (String s : allSubjects) {
            if (!currentSubs.contains(s)) potentialNew.add(s);
        }
        JComboBox<String> cbNew = new JComboBox<>(potentialNew.toArray(new String[0]));
        cbNew.setEnabled(false);

        // Enforce max 3 subjects: if already 3, disable enroll option
        if (currentSubs.size() >= 3) {
            rbEnroll.setEnabled(false);
            rbEnroll.setToolTipText("Already at maximum of 3 subjects; you can only change.");
        }

        JPanel center = new JPanel(new GridLayout(3,2,5,5));
        center.add(rbEnroll);
        center.add(cbNew);
        center.add(rbChange);
        center.add(cbOld);
        add(center, BorderLayout.CENTER);

        rbEnroll.addActionListener(e -> {
            cbNew.setEnabled(true);
            cbOld.setEnabled(false);
        });
        rbChange.addActionListener(e -> {
            cbNew.setEnabled(true);
            cbOld.setEnabled(true);
        });

        JPanel south = new JPanel();
        JButton ok = new JButton("Submit"), cancel = new JButton("Cancel");
        south.add(ok);
        south.add(cancel);
        add(south, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            if (!rbEnroll.isSelected() && !rbChange.isSelected()) {
                JOptionPane.showMessageDialog(this,
                    "Please choose Enroll or Change", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String type = rbEnroll.isSelected() ? "New" : "Change";
            String oldSub = rbChange.isSelected() ? (String)cbOld.getSelectedItem() : "-";
            String newSub = cbNew.getSelectedItem() == null ? "" : cbNew.getSelectedItem().toString();

            if (type.equals("New") && currentSubs.size() >= 3) {
                JOptionPane.showMessageDialog(this,
                    "Cannot enroll new subject: already has 3 subjects.", "Limit Reached",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newSub.isEmpty() || (type.equals("Change") && (oldSub == null || oldSub.isEmpty()))) {
                JOptionPane.showMessageDialog(this,
                    "Please select subject(s)", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String[]> reqs = DataManager.loadAll("requests.txt");
            int next = reqs.stream()
                           .mapToInt(r -> {
                               try { return Integer.parseInt(r[0].substring(1)); }
                               catch (Exception ex) { return 0; }
                           }).max().orElse(0) + 1;
            String reqId = String.format("R%03d", next);

            String date = java.time.LocalDate.now().toString();
            String[] record = {
                reqId,
                studentId,
                type,
                oldSub,
                newSub,
                "PENDING",
                date
            };
            DataManager.append("requests.txt", record);

            JOptionPane.showMessageDialog(this,
                "Request submitted with ID " + reqId,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        cancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }
}


//View requests (Student)
class ViewRequestsDialog extends JDialog {
    public ViewRequestsDialog(JFrame owner, String studentId) {
        super(owner, "View Subject Change Requests", true);

        List<String[]> reqs = DataManager.loadAll("requests.txt");
        String[] cols = {"ReqID", "Type", "Old Subject", "New Subject", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (String[] r : reqs) {
            if (r[1].equals(studentId)) {
                model.addRow(new Object[]{
                    r[0], r[2], r[3], r[4], r[5], r[6]
                });
            }
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        JButton btnDelete = new JButton("Delete Request");
        btnDelete.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Please select a request to delete.");
                return;
            }
            String status = table.getValueAt(sel, 4).toString();
            if (!"PENDING".equals(status)) {
                JOptionPane.showMessageDialog(this, "Only PENDING requests can be deleted.");
                return;
            }
            String reqId = table.getValueAt(sel, 0).toString();

            List<String[]> updated = reqs.stream()
                .filter(r -> !(r[0].equals(reqId) && r[1].equals(studentId)))
                .collect(Collectors.toList());
            DataManager.saveAll("requests.txt", updated);

            model.removeRow(sel);

            JOptionPane.showMessageDialog(this, "Request deleted.");
        });

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());

        JPanel south = new JPanel();
        south.add(btnDelete);
        south.add(btnClose);

        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        setSize(600, 300);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}

//View schedule (Student)
class ViewScheduleDialog extends JDialog {
    private JTable table;

    public ViewScheduleDialog(JFrame owner, String studentId) {
        super(owner, "View Schedule – " + studentId, true);
        setLayout(new BorderLayout(10, 10));

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Schedule ID", "Tutor ID", "Subject", "Date", "Time Range", "Venue"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        String[] stuRec = DataManager.loadAll("students.txt").stream()
            .filter(r -> r.length > 0 && r[0].equals(studentId))
            .findFirst()
            .orElse(new String[0]);

        List<String> enrolled = new ArrayList<>();
        if (stuRec.length >= 9) {
            for (int i = 7; i < stuRec.length - 1; i++) {
                enrolled.add(stuRec[i]);
            }
        }

        for (String[] r : DataManager.loadAll("schedules.txt")) {
            if (r.length < 6) continue;
            String schedId = r[0];
            String tutorId = r[1];
            String dateStr = r[2];
            String timeRange = r[3];
            String venue = r[4];
            String subj = r[5];

            if (!enrolled.contains(subj)) continue;

            model.addRow(new Object[]{
                schedId, tutorId, subj, dateStr, timeRange, venue
            });
        }

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnClose = new JButton("Back");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(600, 400);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}


//Payment status (Student)
class PaymentStatusDialog extends JDialog {
    PaymentStatusDialog(JFrame owner, String studentId) {
        super(owner, "My Payments", true);
        setLayout(new BorderLayout(10,10));

        String[] monthNames = Arrays.stream(Month.values())
            .map(m -> m.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
            .toArray(String[]::new);
        JComboBox<String> cbBillingMonth = new JComboBox<>(monthNames);
        String currentMonthName = LocalDate.now().getMonth()
            .getDisplayName(TextStyle.FULL, Locale.getDefault());
        cbBillingMonth.setSelectedItem(currentMonthName);

        int currentYear = LocalDate.now().getYear();
        Vector<String> years = new Vector<>();
        for (int y = currentYear; y >= currentYear - 5; y--) years.add(String.valueOf(y));
        JComboBox<String> cbBillingYear = new JComboBox<>(years);
        cbBillingYear.setSelectedItem(String.valueOf(currentYear));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Billing Month:"));
        topPanel.add(cbBillingMonth);
        topPanel.add(new JLabel("Year:"));
        topPanel.add(cbBillingYear);
        add(topPanel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"PaymentID","Subject","Billing Month","Billing Year","Paid","Expected","Status","Date/Time"}, 0
        ) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable reload = () -> {
            model.setRowCount(0);
            Map<String, Double> rateMap = new HashMap<>();
            for (String[] c : DataManager.loadAll("charges.txt")) {
                try { rateMap.put(c[0], Double.parseDouble(c[1])); }
                catch (Exception ex) {}
            }

            String[] stuRec = DataManager.loadAll("students.txt").stream()
                .filter(r -> r.length > 0 && r[0].equals(studentId))
                .findFirst()
                .orElse(new String[0]);
            List<String> enrolledSubjects = new ArrayList<>();
            if (stuRec.length >= 8) {
                for (int i = 7; i < stuRec.length - 1; i++) {
                    enrolledSubjects.add(stuRec[i]);
                }
            }

            String billingMon = (String) cbBillingMonth.getSelectedItem();
            String billingYear = (String) cbBillingYear.getSelectedItem();

            for (String subj : enrolledSubjects) {
                Optional<String[]> payOpt = DataManager.loadAll("payments.txt").stream()
                    .filter(p -> p.length >= 6
                        && p[1].equals(studentId)
                        && p[2].equals(subj)
                        && p[4].equalsIgnoreCase(billingMon)
                        && p[5].length() >= 4
                        && p[5].substring(0,4).equals(billingYear))
                    .findFirst();

                String pid = "-";
                String dateTime = "-";
                double paid = 0.0;
                String status;
                if (payOpt.isPresent()) {
                    String[] p = payOpt.get();
                    pid = p[0];
                    try { paid = Double.parseDouble(p[3]); } catch (Exception ex) {}
                    dateTime = p[5];
                    double expectedSubj = rateMap.getOrDefault(subj, 0.0);
                    status = paid + 1e-6 >= expectedSubj ? "Paid" : "Unpaid";
                } else {
                    status = "Unpaid";
                }

                double expected = rateMap.getOrDefault(subj, 0.0);

                model.addRow(new Object[]{
                    pid,
                    subj,
                    billingMon,
                    billingYear,
                    String.format("%.2f", paid),
                    String.format("%.2f", expected),
                    status,
                    dateTime
                });
            }
        };

        cbBillingMonth.addActionListener(e -> reload.run());
        cbBillingYear.addActionListener(e -> reload.run());
        reload.run();

        JPanel btnPanel = new JPanel();
        JButton btnPay  = new JButton("Add Payment");
        JButton btnClose= new JButton("Close");
        btnPanel.add(btnPay);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);

        btnPay.addActionListener(e -> {
            new MakePaymentDialog(owner, studentId,
                (String)cbBillingMonth.getSelectedItem(),
                (String)cbBillingYear.getSelectedItem());
            reload.run();
        });
        btnClose.addActionListener(e -> dispose());

        setSize(750, 450);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}



//Make payment(student)
class MakePaymentDialog extends JDialog {
    String[] monthNames = Arrays.stream(Month.values())
        .map(m -> m.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
        .toArray(String[]::new);

    public MakePaymentDialog(JFrame owner, String studentId, String preselectedMonth, String preselectedYear) {
        super(owner, "Make Payment", true);
        setLayout(new BorderLayout(5,5));

        JComboBox<String> cbMonth = new JComboBox<>(monthNames);
        cbMonth.setSelectedItem(preselectedMonth);
        int currentYear = LocalDate.now().getYear();
        Vector<String> years = new Vector<>();
        for (int y = currentYear; y >= currentYear - 5; y--) years.add(String.valueOf(y));
        JComboBox<String> cbYear = new JComboBox<>(years);
        cbYear.setSelectedItem(preselectedYear);

        String[] stuRec = DataManager.loadAll("students.txt").stream()
            .filter(r -> r.length > 0 && r[0].equals(studentId))
            .findFirst()
            .orElse(new String[0]);

        List<String> enrolled = new ArrayList<>();
        if (stuRec.length >= 8) {
            for (int i = 7; i < stuRec.length - 1; i++) {
                enrolled.add(stuRec[i]);
            }
        }

        JComboBox<String> cbSubj = new JComboBox<>(enrolled.toArray(new String[0]));

        JTextField tfAmount = new JTextField(10);
        tfAmount.setEditable(false);

        Map<String, Double> rateMap = new HashMap<>();
        for (String[] c : DataManager.loadAll("charges.txt")) {
            try { rateMap.put(c[0], Double.parseDouble(c[1])); } catch (Exception ignored) {}
        }

        cbSubj.addActionListener(e -> {
            String subj = (String) cbSubj.getSelectedItem();
            if (subj != null) {
                tfAmount.setText(String.format("%.2f", rateMap.getOrDefault(subj, 0.0)));
            }
        });
        if (cbSubj.getItemCount() > 0) cbSubj.setSelectedIndex(0);

        String nextId = "P" + String.format("%03d",
            DataManager.loadAll("payments.txt").size() + 1);
        JTextField tfId = new JTextField(nextId, 8);
        tfId.setEditable(false);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String now = LocalDateTime.now().format(dtf);
        JTextField tfDT = new JTextField(now, 16);
        tfDT.setEditable(false);

        JPanel form = new JPanel(new GridLayout(7,2,5,5));
        form.add(new JLabel("Payment ID:")); form.add(tfId);
        form.add(new JLabel("Student ID:")); form.add(new JLabel(studentId));
        form.add(new JLabel("Billing Month:")); form.add(cbMonth);
        form.add(new JLabel("Billing Year:")); form.add(cbYear);
        form.add(new JLabel("Subject:")); form.add(cbSubj);
        form.add(new JLabel("Amount:")); form.add(tfAmount);
        form.add(new JLabel("Date/Time:")); form.add(tfDT);
        add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton ok = new JButton("OK"), cancel = new JButton("Cancel");
        btns.add(ok); btns.add(cancel);
        add(btns, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            String pid = tfId.getText().trim();
            String subj = (String) cbSubj.getSelectedItem();
            if (subj == null) {
                JOptionPane.showMessageDialog(this, "No subject selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String mon = (String) cbMonth.getSelectedItem();
            String yr  = (String) cbYear.getSelectedItem();
            String amt = tfAmount.getText().trim();
            String ts  = tfDT.getText().trim();

            boolean already = DataManager.loadAll("payments.txt").stream().anyMatch(p ->
                p.length >= 6 &&
                p[1].equals(studentId) &&
                p[2].equals(subj) &&
                p[4].equalsIgnoreCase(mon) &&
                p[5].length() >= 4 &&
                p[5].substring(0,4).equals(yr)
            );
            if (already) {
                JOptionPane.showMessageDialog(this, "Already paid for this subject in selected billing period.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DataManager.append("payments.txt", new String[]{
                pid, studentId, subj, amt, mon, ts
            });

            JOptionPane.showMessageDialog(this,
                "Payment recorded.\n" +
                "Subject: " + subj + "\n" +
                "Billing: " + mon + " " + yr + "\n" +
                "Amount: RM " + amt,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
        cancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }
}



//Course management (Tutor)
class ScheduleManagementDialog extends JDialog {
    private JTable table;
    private String tutorId;

    public ScheduleManagementDialog(JFrame owner, String tutorId) {
        super(owner, "Course Management – " + tutorId, true);
        this.tutorId = tutorId;
        setLayout(new BorderLayout(10,10));

        final List<String> tutorSubjects;
        {
            List<String> tmp = new ArrayList<>();
            for (String[] t : DataManager.loadAll("tutors.txt")) {
                if (t[0].equals(tutorId)) {
                    if (t.length >= 4) {
                        tmp = new ArrayList<>(Arrays.asList(t[3].split(";")));
                    }
                    break;
                }
            }
            tutorSubjects = tmp;
        }

        List<String[]> raw = DataManager.loadAll("schedules.txt").stream()
            .filter(r -> r.length > 1 && r[1].equals(tutorId))
            .collect(Collectors.toList());
        String[] cols = { "ID", "Tutor ID", "Date", "TimeRange", "Venue", "Subject" };
        String[][] rows = new String[raw.size()][cols.length];
        for (int i = 0; i < raw.size(); i++) {
            String[] r = raw.get(i);
            String timeRange = r.length >= 4 ? r[3] : "";
            String venue = r.length >= 5 ? r[4] : "";
            String subject = r.length >= 6 ? r[5] : "";
            rows[i] = new String[] {
                r[0],
                r[1],
                r.length >= 3 ? r[2] : "",
                timeRange,
                venue,
                subject
            };
        }
        table = new JTable(rows, cols);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton addBtn   = new JButton("Add");
        JButton editBtn  = new JButton("Edit");
        JButton delBtn   = new JButton("Delete");
        JButton closeBtn = new JButton("Back");
        btns.add(addBtn);
        btns.add(editBtn);
        btns.add(delBtn);
        btns.add(closeBtn);
        add(btns, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null, tutorSubjects));

        editBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this,
                    "Please select a schedule to edit",
                    "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = table.getValueAt(sel, 0).toString();
            openForm(id, tutorSubjects);
        });

        delBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this,
                    "Please select a schedule to delete",
                    "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = table.getValueAt(sel, 0).toString();
            if (JOptionPane.showConfirmDialog(this,
                    "Delete schedule " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
                DataManager.delete("schedules.txt", 0, id);
                dispose();
                new ScheduleManagementDialog((JFrame) getOwner(), tutorId);
            }
        });

        closeBtn.addActionListener(e -> dispose());

        setSize(750, 450);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void openForm(String schedId, List<String> tutorSubjects) {
        boolean isNew = (schedId == null);

        JTextField tfId = new JTextField(isNew ? genId() : schedId, 8);
        tfId.setEditable(false);
        JTextField tfTutor = new JTextField(tutorId, 8);
        tfTutor.setEditable(false);
        JTextField tfDate  = new JTextField(10); // yyyy-MM-dd

        String[] hourArr = IntStream.rangeClosed(8, 22)
                            .mapToObj(h -> String.format("%02d", h))
                            .toArray(String[]::new);
        String[] minuteArr = IntStream.range(0, 60)
                              .mapToObj(m -> String.format("%02d", m))
                              .toArray(String[]::new);

        JComboBox<String> cbStartHour = new JComboBox<>(hourArr);
        JComboBox<String> cbStartMinute = new JComboBox<>(minuteArr);
        JComboBox<String> cbEndHour = new JComboBox<>(hourArr);
        JComboBox<String> cbEndMinute = new JComboBox<>(minuteArr);

        String[] rooms = { "Room1", "Room2", "Room3", "Room4", "Room5" };
        JComboBox<String> cbVenue = new JComboBox<>(rooms);

        JComboBox<String> cbSubject = new JComboBox<>(tutorSubjects.toArray(new String[0]));
        if (cbSubject.getItemCount() > 0) cbSubject.setSelectedIndex(0);

        if (!isNew) {
            for (String[] r : DataManager.loadAll("schedules.txt")) {
                if (r[0].equals(schedId)) {
                    if (r.length >= 3) tfDate.setText(r[2]);
                    String timeRange = r.length >= 4 ? r[3] : "";
                    if (timeRange.contains("-")) {
                        String[] parts = timeRange.split("-");
                        String[] start = parts[0].split(":");
                        String[] end = parts[1].split(":");
                        if (start.length == 2) {
                            cbStartHour.setSelectedItem(start[0]);
                            cbStartMinute.setSelectedItem(start[1]);
                        }
                        if (end.length == 2) {
                            cbEndHour.setSelectedItem(end[0]);
                            cbEndMinute.setSelectedItem(end[1]);
                        }
                    } else {
                        String[] t = timeRange.split(":");
                        if (t.length == 2) {
                            cbStartHour.setSelectedItem(t[0]);
                            cbStartMinute.setSelectedItem(t[1]);
                            cbEndHour.setSelectedItem(t[0]);
                            cbEndMinute.setSelectedItem(t[1]);
                        }
                    }
                    if (r.length >= 5) cbVenue.setSelectedItem(r[4]);
                    if (r.length >= 6) cbSubject.setSelectedItem(r[5]);
                    break;
                }
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,8,5,8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Schedule ID:"), gbc);
        gbc.gridx=1;               form.add(tfId, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Tutor ID:"), gbc);
        gbc.gridx=1;               form.add(tfTutor, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx=1;               form.add(tfDate, gbc); y++;

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        timePanel.add(new JLabel("Start:"));
        timePanel.add(cbStartHour);
        timePanel.add(new JLabel(":"));
        timePanel.add(cbStartMinute);
        timePanel.add(Box.createHorizontalStrut(10));
        timePanel.add(new JLabel("End:"));
        timePanel.add(cbEndHour);
        timePanel.add(new JLabel(":"));
        timePanel.add(cbEndMinute);
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Time Range:"), gbc);
        gbc.gridx=1;               form.add(timePanel, gbc); y++;

        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Venue:"), gbc);
        gbc.gridx=1;               form.add(cbVenue, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Subject:"), gbc);
        gbc.gridx=1;               form.add(cbSubject, gbc); y++;

        int res = JOptionPane.showConfirmDialog(this, form,
            isNew ? "Add Schedule" : "Edit Schedule",
            JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String id = tfId.getText().trim();
        String date = tfDate.getText().trim();
        String start = cbStartHour.getSelectedItem() + ":" + cbStartMinute.getSelectedItem();
        String end = cbEndHour.getSelectedItem() + ":" + cbEndMinute.getSelectedItem();

        if (start.compareTo(end) >= 0) {
            JOptionPane.showMessageDialog(this,
                "End time must be after start time.", "Time Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String timeRange = start + "-" + end;
        String venue = (String) cbVenue.getSelectedItem();
        String subject = (String) cbSubject.getSelectedItem();

        // conflict detection: same date + venue + overlapping time (skip self if editing)
        List<String[]> all = DataManager.loadAll("schedules.txt");
        for (String[] rec : all) {
            if (rec.length < 5) continue;
            if (!rec[2].equals(date)) continue;
            if (!rec[4].equals(venue)) continue;
            if (!isNew && rec[0].equals(id)) continue;

            String existing = rec.length >= 4 ? rec[3] : "";
            String existStart, existEnd;
            if (existing.contains("-")) {
                String[] sp = existing.split("-");
                existStart = sp[0];
                existEnd = sp[1];
            } else {
                existStart = existing;
                existEnd = existing;
            }

            if (!(end.compareTo(existStart) <= 0 || start.compareTo(existEnd) >= 0)) {
                JOptionPane.showMessageDialog(this,
                    "Conflict: " + venue + " has overlapping schedule on " + date + " (" + existing + ").",
                    "Scheduling Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String[] record = new String[]{ id, tutorId, date, timeRange, venue, subject };
        if (isNew) DataManager.append("schedules.txt", record);
        else       DataManager.update("schedules.txt", 0, schedId, record);

        dispose();
        new ScheduleManagementDialog((JFrame)getOwner(), tutorId);
    }

    private String genId() {
        List<String[]> all = DataManager.loadAll("schedules.txt");
        int max = all.stream().mapToInt(r -> {
            try { return Integer.parseInt(r[0].substring(1)); }
            catch (Exception e) { return 0; }
        }).max().orElse(0);
        return String.format("C%03d", max + 1);
    }
}



//View students (Tutor)
class StudentViewDialog extends JDialog {
    private JTable table;

    private static final List<String> LEVEL_ORDER = Arrays.asList("F1","F2","F3","F4","F5");

    private boolean levelMatches(String tutorLevel, String studentLevel) {
        if (tutorLevel == null || tutorLevel.isEmpty()) return false;
        studentLevel = studentLevel.trim().toUpperCase();
        tutorLevel = tutorLevel.trim().toUpperCase();
        if (tutorLevel.contains("-")) {
            String[] parts = tutorLevel.split("-");
            if (parts.length != 2) return tutorLevel.equalsIgnoreCase(studentLevel);
            String low = parts[0];
            String high = parts[1];
            int li = LEVEL_ORDER.indexOf(low);
            int hi = LEVEL_ORDER.indexOf(high);
            int si = LEVEL_ORDER.indexOf(studentLevel);
            if (li == -1 || hi == -1 || si == -1) return false;
            return si >= Math.min(li, hi) && si <= Math.max(li, hi);
        } else {
            return tutorLevel.equalsIgnoreCase(studentLevel);
        }
    }

    StudentViewDialog(JFrame owner, String tutorIdentifier) {
        super(owner, "View Students – " + tutorIdentifier, true);
        setLayout(new BorderLayout(10,10));

        String actualTutorId = null;
        for (String[] t : DataManager.loadAll("tutors.txt")) {
            if (t.length > 0 && t[0].equals(tutorIdentifier)) {
                actualTutorId = t[0];
                break;
            }
        }
        if (actualTutorId == null) {
            for (String[] u : DataManager.loadAll("users.txt")) {
                if (u.length >= 4 && u[0].equals(tutorIdentifier) && u[2].equalsIgnoreCase("Tutor")) {
                    String tutorIdFromUser = u[3];
                    for (String[] t : DataManager.loadAll("tutors.txt")) {
                        if (t.length > 0 && t[0].equals(tutorIdFromUser)) {
                            actualTutorId = t[0];
                            break;
                        }
                    }
                    if (actualTutorId != null) break;
                }
            }
        }
        if (actualTutorId == null) {
            JOptionPane.showMessageDialog(owner,
                "Tutor record not found for " + tutorIdentifier,
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        String tutorLevel = "";
        List<String> tutorSubjects = new ArrayList<>();
        for (String[] t : DataManager.loadAll("tutors.txt")) {
            if (t.length > 0 && t[0].equals(actualTutorId)) {
                if (t.length >= 3) tutorLevel = t[2].trim();
                if (t.length >= 4) {
                    String rawSubs = t[3];
                    for (String s : rawSubs.split(";")) {
                        String trimmed = s.trim();
                        if (!trimmed.isEmpty()) tutorSubjects.add(trimmed);
                    }
                }
                break;
            }
        }

        String[] cols = {
            "ID","Name","IC","Email","Phone",
            "Address","Level","Subjects","Enrollment Month"
        };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        for (String[] s : DataManager.loadAll("students.txt")) {
            if (s.length < 8) continue;

            String studentId = s[0];
            String studentName = s[1];
            String studentIC = s[2];
            String studentEmail = s[3];
            String studentPhone = s[4];
            String studentAddress = s[5];
            String studentLevel = s[6].trim();

            List<String> studentSubs = new ArrayList<>();
            for (int i = 7; i < s.length - 1; i++) {
                if (!s[i].trim().isEmpty()) studentSubs.add(s[i].trim());
            }


            if (!levelMatches(tutorLevel, studentLevel)) {;
                continue;
            }

            boolean hasOverlap = false;
            for (String ts : tutorSubjects) {
                for (String ss : studentSubs) {
                    if (ts.equalsIgnoreCase(ss)) {
                        hasOverlap = true;
                        break;
                    }
                }
                if (hasOverlap) break;
            }
            if (!hasOverlap) {;
                continue;
            }

            String subjList = String.join(", ", studentSubs);
            String month = s[s.length - 1];
            model.addRow(new Object[]{
                studentId, studentName, studentIC, studentEmail, studentPhone,
                studentAddress, studentLevel, subjList, month
            });
        }

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnClose = new JButton("Back");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(800, 400);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}



//Change charges (Tutor)
class ChangeChargesDialog extends JDialog {
    public ChangeChargesDialog(JFrame owner, String tutorId) {
        super(owner, "Change Charges – " + tutorId, true);
        setLayout(new BorderLayout(10,10));

        List<String> subjects = new ArrayList<>();
        for (String[] r : DataManager.loadAll("tutors.txt")) {
            if (r[0].equals(tutorId)) {
                if (r.length >= 4) {
                    String subjField = r[3];
                    if (!subjField.isBlank()) {
                        subjects = Arrays.asList(subjField.split(";"));
                    }
                }
                break;
            }
        }

        JComboBox<String> cbSubject = new JComboBox<>(subjects.toArray(new String[0]));
        JTextField tfCharge = new JTextField(10);
        tfCharge.setEditable(true);

        Runnable loadCharge = () -> {
            String subject = (String) cbSubject.getSelectedItem();
            String amt = "";
            for (String[] r : DataManager.loadAll("charges.txt")) {
                if (r.length >= 2 && r[0].equalsIgnoreCase(subject)) {
                    amt = r[1];
                    break;
                }
            }
            tfCharge.setText(amt);
        };
        cbSubject.addActionListener(e -> loadCharge.run());
        if (cbSubject.getItemCount() > 0) {
            cbSubject.setSelectedIndex(0);
            loadCharge.run();
        }

        JPanel form = new JPanel(new GridLayout(3,2,5,5));
        form.add(new JLabel("Tutor ID:")); form.add(new JTextField(tutorId, 8){{setEditable(false);}});
        form.add(new JLabel("Subject:"));  form.add(cbSubject);
        form.add(new JLabel("Charge:"));   form.add(tfCharge);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            String subject = (String) cbSubject.getSelectedItem();
            String amt = tfCharge.getText().trim();
            if (subject == null || subject.isBlank()) {
                JOptionPane.showMessageDialog(this, "No subject selected.",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Double.parseDouble(amt);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid charge. Please enter a numeric value.",
                                              "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String[]> all = DataManager.loadAll("charges.txt");
            boolean found = false;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i)[0].equalsIgnoreCase(subject)) {
                    all.set(i, new String[]{subject, amt});
                    found = true;
                    break;
                }
            }
            if (!found) {
                all.add(new String[]{subject, amt});
            }
            DataManager.saveAll("charges.txt", all);

            JOptionPane.showMessageDialog(this, "Charge updated for " + subject + ".",
                                          "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }
}

//Profile(all roles)
class ProfileDialog extends JDialog {
    private final String[] userRec;
    private final String role;
    // Tutor
    private String savedTutorId, savedTutorLevel = "", savedTutorSubject = "";
    private List<JCheckBox> subjChecks;

    private JTextField tfName, tfIC, tfEmail, tfPhone, tfAddress, tfMonth;
    private JComboBox<String> cbLevel, cbSubject;
    private JPanel subjPanel;
    private JPasswordField pfPass;
    private JButton btnEdit, btnSave, btnCancel;

    public ProfileDialog(JFrame owner, String username, String role) {
        super(owner, "Profile – " + username, true);
        this.role = role;

        List<String[]> users = DataManager.loadAll("users.txt");
        String[] found = users.stream()
                              .filter(r -> r[0].equals(username))
                              .findFirst().orElse(null);
        if (found == null) {
            JOptionPane.showMessageDialog(owner,
                "User not found", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            userRec = new String[0];
            return;
        }
        userRec = found;

        JTextField tfUser   = new JTextField(userRec[0], 15);
        tfUser.setEditable(false);
        JTextField tfRole   = new JTextField(role, 15);
        tfRole.setEditable(false);
        pfPass = new JPasswordField(15);

        tfName    = new JTextField(15);
        tfIC      = new JTextField(15);
        tfEmail   = new JTextField(15);
        tfPhone   = new JTextField(15);
        tfAddress = new JTextField(15);
        cbLevel   = new JComboBox<>(new String[]{"F1","F2","F3","F4","F5"});
        cbSubject = new JComboBox<>();
        subjPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfMonth   = new JTextField(8);

        btnEdit   = new JButton("Edit");
        btnSave   = new JButton("Save");
        btnCancel = new JButton("Cancel");

        JPanel top = new JPanel(new GridLayout(3,2,5,5));
        top.add(new JLabel("Username:"));     top.add(tfUser);
        top.add(new JLabel("Role:"));         top.add(tfRole);
        top.add(new JLabel("New Password:")); top.add(pfPass);

        JPanel details = new JPanel(new GridLayout(0,2,5,5));
        details.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        if ("Receptionist".equals(role)) {
            tfName.setText(userRec.length > 4 ? userRec[4] : "");
            tfPhone.setText(userRec.length > 7 ? userRec[7] : "");
            tfIC.setText(userRec.length > 5 ? userRec[5] : "");
            tfEmail.setText(userRec.length > 6 ? userRec[6] : "");
            tfAddress.setText(userRec.length > 8 ? userRec[8] : "");

            details.add(new JLabel("Name:"));    details.add(tfName);
            details.add(new JLabel("IC:"));      details.add(tfIC);
            details.add(new JLabel("Email:"));   details.add(tfEmail);
            details.add(new JLabel("Phone:"));   details.add(tfPhone);
            details.add(new JLabel("Address:")); details.add(tfAddress);
        }

        else {
            tfName   .setText(userRec.length>4 ? userRec[4] : "");
            tfIC     .setText(userRec.length>5 ? userRec[5] : "");
            tfEmail  .setText(userRec.length>6 ? userRec[6] : "");
            tfPhone  .setText(userRec.length>7 ? userRec[7] : "");
            tfAddress.setText(userRec.length>8 ? userRec[8] : "");

            details.add(new JLabel("Name:"));    details.add(tfName);
            details.add(new JLabel("IC:"));      details.add(tfIC);
            details.add(new JLabel("Email:"));   details.add(tfEmail);
            details.add(new JLabel("Phone:"));   details.add(tfPhone);
            details.add(new JLabel("Address:")); details.add(tfAddress);

            if ("Tutor".equals(role)) {
                String[] row = null;
                for (String[] r : DataManager.loadAll("tutors.txt")) {
                    if (r[0].equals(userRec[0])) {
                        row = r;
                        savedTutorId = r[0];
                        break;
                    }
                }
                String lvl = (row != null && row.length >= 3) ? row[2] : "F1-F3";
                cbLevel.setModel(new DefaultComboBoxModel<>(new String[]{"F1-F3", "F4-F5"}));
                cbLevel.setSelectedItem(lvl);

                subjPanel.removeAll();
                subjChecks = new ArrayList<>();
                Set<String> tutorSubs = (row != null && row.length >= 4 && !row[3].isEmpty())
                    ? Arrays.stream(row[3].split(";")).collect(Collectors.toSet())
                    : Collections.emptySet();

                List<String> available;
                if ("F1-F3".equals(lvl)) {
                    available = Arrays.asList("BM","BI","Math","Science","BC","History");
                } else { 
                    available = Arrays.asList("BM","BI","Math","Science","BC","History",
                                              "Physics","Chemistry","Biology","Accounting","AddMath");
                }
                for (String sub : available) {
                    JCheckBox cb = new JCheckBox(sub, tutorSubs.contains(sub));
                    subjChecks.add(cb);
                    subjPanel.add(cb);
                }

                details.add(new JLabel("Level Range:"));    details.add(cbLevel);
                details.add(new JLabel("Subjects:"));       details.add(subjPanel);

                cbLevel.addActionListener(e -> {
                    String sel = (String) cbLevel.getSelectedItem();
                    subjPanel.removeAll();
                    subjChecks.clear();
                    List<String> avail;
                    if ("F1-F3".equals(sel)) {
                        avail = Arrays.asList("BM","BI","Math","Science","BC","History");
                    } else {
                        avail = Arrays.asList("BM","BI","Math","Science","BC","History",
                                              "Physics","Chemistry","Biology","Accounting","AddMath");
                    }
                    for (String s : avail) {
                        JCheckBox cb = new JCheckBox(s);
                        if (tutorSubs.contains(s)) cb.setSelected(true);
                        subjChecks.add(cb);
                        subjPanel.add(cb);
                    }
                    subjPanel.revalidate();
                    subjPanel.repaint();
                });
            }

            else if ("Student".equals(role)) {
                for (String[] r : DataManager.loadAll("students.txt")) {
                    if (r[0].equals(userRec[0])) {
                        tfName   .setText(r[1]);
                        tfIC     .setText(r[2]);
                        tfEmail  .setText(r[3]);
                        tfPhone  .setText(r[4]);
                        tfAddress.setText(r[5]);
                        cbLevel  .setSelectedItem(r[6]);
                        subjPanel.removeAll();
                        for (int i = 7; i < r.length - 1; i++) {
                            JCheckBox cb = new JCheckBox(r[i], true);
                            subjPanel.add(cb);
                        }
                        tfMonth.setText(r[r.length - 1]);
                        break;
                    }
                }
                details.add(new JLabel("Level:"));    details.add(cbLevel);
                details.add(new JLabel("Subjects:")); details.add(subjPanel);
                details.add(new JLabel("Month:"));    details.add(tfMonth);
            }
        }

        setFieldsEditable(false);

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnEdit);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        btnSave.setVisible(false);
        btnCancel.setVisible(false);

        getContentPane().add(top,     BorderLayout.NORTH);
        getContentPane().add(details, BorderLayout.CENTER);
        getContentPane().add(btnPanel,BorderLayout.SOUTH);

        btnEdit.addActionListener(e -> {
            btnEdit.setVisible(false);
            btnSave.setVisible(true);
            btnCancel.setVisible(true);
            setFieldsEditable(true);
        });
        btnCancel.addActionListener(e -> {
            dispose();
            new ProfileDialog(owner, userRec[0], role);
        });
        btnSave.addActionListener(e -> {
            String newPass = new String(pfPass.getPassword()).trim();
            String passUse = newPass.isEmpty() ? userRec[1] : newPass;
            String[] updUser = {
                userRec[0],
                passUse,
                userRec[2],
                userRec[3],
                tfName.getText().trim(),
                tfIC.getText().trim(),
                tfEmail.getText().trim(),
                tfPhone.getText().trim(),
                tfAddress.getText().trim()
            };
            DataManager.update("users.txt", 0, userRec[0], updUser);

            if ("Receptionist".equals(role)) {
        String[] updRec = new String[] {
            userRec[0],
            tfName.getText().trim(),
            tfPhone.getText().trim()
        };
        DataManager.update("receptionists.txt", 0, userRec[0], updRec);
            }
            else if ("Tutor".equals(role)) {
                List<String> selectedSubs = subjChecks.stream()
                    .filter(JCheckBox::isSelected)
                    .map(JCheckBox::getText)
                    .collect(Collectors.toList());
                String subjRec = String.join(";", selectedSubs);
                String[] updT = {
                    savedTutorId,
                    tfName.getText().trim(),
                    cbLevel.getSelectedItem().toString(),
                    subjRec,
                    tfPhone.getText().trim()
                };
                DataManager.update("tutors.txt", 0, savedTutorId, updT);
            }
            else if ("Student".equals(role)) {
                List<String> subs = new ArrayList<>();
                for (Component c : subjPanel.getComponents()) {
                    if (c instanceof JCheckBox && ((JCheckBox)c).isSelected()) {
                        subs.add(((JCheckBox)c).getText());
                    }
                }
                List<String> rec = new ArrayList<>(Arrays.asList(
                    userRec[0],
                    tfName.getText().trim(),
                    tfIC.getText().trim(),
                    tfEmail.getText().trim(),
                    tfPhone.getText().trim(),
                    tfAddress.getText().trim(),
                    cbLevel.getSelectedItem().toString()
                ));
                rec.addAll(subs);
                rec.add(tfMonth.getText().trim());
                DataManager.update("students.txt", 0, userRec[0],
                                   rec.toArray(new String[0]));
            }

            JOptionPane.showMessageDialog(this,
                "Profile saved.", "Done", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }

    private void rebuildSubjects() {
        cbSubject.removeAllItems();
        for (String s : new String[]{"BM","BI","Math","Science","BC","History"})
            cbSubject.addItem(s);
        String lvl = (String)cbLevel.getSelectedItem();
        if ("F4".equals(lvl) || "F5".equals(lvl)) {
            for (String s : new String[]{"Physics","Chemistry","Biology","Account","Addmath"})
                cbSubject.addItem(s);
        }
        cbSubject.setSelectedItem(savedTutorSubject);
    }

    private void setFieldsEditable(boolean e) {
        pfPass   .setEnabled(e);
        tfName   .setEditable(e);
        tfIC     .setEditable(e);
        tfEmail  .setEditable(e);
        tfPhone  .setEditable(e);
        tfAddress.setEditable(e);

        tfMonth.setEditable(false);

        boolean isAdmin = "Admin".equals(role);
        cbLevel.setEnabled(e && isAdmin);
        for (Component c : subjPanel.getComponents()) {
            if (c instanceof JCheckBox) {
                c.setEnabled(e && isAdmin);
            }
        }
        cbSubject.setEnabled(false);
    }
}
}