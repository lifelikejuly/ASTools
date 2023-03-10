package com.julyyu.asplugins.tools.finduseless.ui;

import com.julyyu.asplugins.tools.finduseless.ResFileInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Enumeration;
import java.util.List;

public class DeleteAllFilesDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList<ResFileInfo> fileList;
    private DefaultListModel<ResFileInfo> fileModel;

    public DeleteAllFilesDialog(java.util.List<ResFileInfo> files) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        fileModel = new DefaultListModel();
        fileModel.addAll(files);
        fileList.setModel(fileModel);
        fileList.setAutoscrolls(true);
        fileList.setCellRenderer(createListOutputInfoRenderer());

        setSize(800, 400);
        setLocationRelativeTo(null);
    }

    private ListCellRenderer createListOutputInfoRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File file = ((ResFileInfo) value).getResFile();
                try {
                    setIcon(new ImageIcon(file.getAbsolutePath()));
                }catch (Exception e){

                }
                setText(file.getPath());
                return c;
            }
        };
    }

    private void onOK() {
        try {

            Enumeration<ResFileInfo> fileEnumeration = fileModel.elements();

            while (fileEnumeration.hasMoreElements()){
                File file = fileEnumeration.nextElement().getResFile();
                if (file != null && file.exists()) file.delete();
            }
        }catch (Exception e){

        }finally {
            dispose();
        }
    }

    private void onCancel() {
        dispose();
    }

    public static void showDeleteAllFilesDialog(List<ResFileInfo> fileList) {
        DeleteAllFilesDialog dialog = new DeleteAllFilesDialog(fileList);
        dialog.setVisible(true);
    }
}
