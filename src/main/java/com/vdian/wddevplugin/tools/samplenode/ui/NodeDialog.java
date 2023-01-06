package com.vdian.wddevplugin.tools.samplenode.ui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.vdian.wddevplugin.tools.samplenode.PageNodeWriter;
import com.vdian.wddevplugin.tools.samplenode.WidgetNodeWriter;
import com.vdian.wddevplugin.tools.samplenode.model.NodeEntity;
import com.vdian.wddevplugin.utils.ClsUtils;

import javax.swing.*;
import java.awt.event.*;

public class NodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JRadioButton pageNodeRadioButton;
    private JRadioButton widgetNodeRadioButton;


    private PsiDirectory parentFolder;
    private Project project;

    public NodeDialog(Project project, PsiDirectory parentFolder) {
        this.project = project;
        this.parentFolder = parentFolder;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(600, 400);
        setLocationRelativeTo(null);
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

        pageNodeRadioButton.setSelected(true);
        widgetNodeRadioButton.setSelected(false);
        pageNodeRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pageNodeRadioButton.setSelected(true);
                widgetNodeRadioButton.setSelected(false);
            }
        });
        widgetNodeRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pageNodeRadioButton.setSelected(false);
                widgetNodeRadioButton.setSelected(true);
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
    }

    private void onOK() {
        if (textField1.getText() == null || "".equals(textField1.getText().trim())) {
            return;
        }
        if(pageNodeRadioButton.isSelected()){
            createPageNode();
        }else{
            createWidgetNode();
        }
        dispose();
    }

    private void createPageNode(){
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                String fileName = this.textField1.getText().trim();

                String prefix = textField1.getText().replaceAll("[A-Z]", "_$0").toLowerCase();
                if(prefix.startsWith("_")){
                    prefix = prefix.substring(1);
                }
                PsiDirectory psiDirectory = parentFolder.createSubdirectory(prefix);
                prefix += "_sample.dart";
                PsiFile dartFile = psiDirectory.createFile(prefix);
                NodeEntity nodeEntity = new NodeEntity();
                nodeEntity.setClassName(fileName);
                nodeEntity.setFileName(prefix);
                ClsUtils clsUtils = new ClsUtils();
                nodeEntity.setDir(clsUtils.getClsDir(dartFile));
                PageNodeWriter pageNodeWriter = new PageNodeWriter(project, nodeEntity,dartFile);
                pageNodeWriter.start();
                dispose();
            } catch (Exception exception) {
//                showTip("Json text format error");
            }
        });
    }

    private void createWidgetNode(){
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                String fileName = this.textField1.getText().trim();

                String prefix = textField1.getText().replaceAll("[A-Z]", "_$0").toLowerCase();
                if(prefix.startsWith("_")){
                    prefix = prefix.substring(1);
                }
                prefix += ".dart";
                PsiFile dartFile = parentFolder.createFile(prefix);
                NodeEntity nodeEntity = new NodeEntity();
                nodeEntity.setClassName(fileName);
                nodeEntity.setFileName(prefix);
                ClsUtils clsUtils = new ClsUtils();
                nodeEntity.setDir(clsUtils.getClsDir(dartFile));
                WidgetNodeWriter widgetNodeWriter = new WidgetNodeWriter(project, nodeEntity,dartFile);
                widgetNodeWriter.start();
                dispose();
            } catch (Exception exception) {
//                showTip("Json text format error");
            }
        });
    }

    private void onCancel() {
        dispose();
    }

}