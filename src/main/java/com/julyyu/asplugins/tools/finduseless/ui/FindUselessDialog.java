package com.julyyu.asplugins.tools.finduseless.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.julyyu.asplugins.tools.finduseless.OutPutInfo;
import com.julyyu.asplugins.tools.finduseless.ResFileInfo;
import com.julyyu.asplugins.tools.finduseless.FindUselessUtils;
import com.julyyu.asplugins.ui.checkboxtrees.CheckBoxTreeCellRenderer;
import com.julyyu.asplugins.ui.checkboxtrees.CheckBoxTreeNode;
import com.julyyu.asplugins.utils.FileUtils;
import com.julyyu.asplugins.utils.ProjectUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindUselessDialog extends JDialog {

    private List<String> ignoreDirs = new ArrayList<>();


    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList<OutPutInfo> outputInfoList;

    private JButton deleteAll;

    private DefaultListModel<OutPutInfo> infoOutPutList;

    private JTree findDirTree;
    private DefaultTreeModel findTreeModel;
    private CheckBoxTreeNode findRootNode;

    private JTree dirTree;
    private DefaultTreeModel dirTreeModel;
    private CheckBoxTreeNode rootNode;

    private Project activeProject;

    private List<ResFileInfo> findResFiles = new ArrayList<>();
    private Set<ResFileInfo> usefulFiles = new HashSet<>();
    private List<File> resTextFiles = new ArrayList<>();


    public FindUselessDialog() {
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
        deleteAll.setVisible(false);
        deleteAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DeleteAllFilesDialog.showDeleteAllFilesDialog(findResFiles);
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

        setSize(1000, 750);
        setLocationRelativeTo(null);


        // ????????????
        infoOutPutList = new DefaultListModel();
        outputInfoList.setModel(infoOutPutList);
        outputInfoList.setAutoscrolls(true);
        outputInfoList.setCellRenderer(createListOutputInfoRenderer());
        outputInfoList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JList<OutPutInfo> list = (JList) e.getSource();
                OutPutInfo outPutInfo = list.getSelectedValue();
                File file = outPutInfo.getFile();
                if (file != null) {
                    DelectFileDialog.showDeleteFileDialog(file);
                }
            }
        });


        // ????????????
        dirTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    CheckBoxTreeNode node = (CheckBoxTreeNode) path.getLastPathComponent();
                    if (node != null) {
                        boolean isSelected = !node.isSelected();
                        node.setSelected(isSelected);
                        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                    }
                }
                tree.removeSelectionPath(path);
            }
        });
        dirTree.setCellRenderer(new CheckBoxTreeCellRenderer());
        dirTree.setShowsRootHandles(true);
        activeProject = ProjectUtils.findProjectDir();

        rootNode = new CheckBoxTreeNode();
        dirTreeModel = new DefaultTreeModel(rootNode);
        dirTree.setModel(dirTreeModel);
        FindUselessUtils.loopDirTrees(rootNode, activeProject, ignoreDirs);

        // ????????????????????????
        findDirTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    CheckBoxTreeNode node = (CheckBoxTreeNode) path.getLastPathComponent();
                    if (node != null) {
                        boolean isSelected = !node.isSelected();
                        node.setSelected(isSelected);
                        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                    }
                }
                tree.removeSelectionPath(path);
            }
        });
        findDirTree.setCellRenderer(new CheckBoxTreeCellRenderer());
        findDirTree.setShowsRootHandles(true);
        activeProject = ProjectUtils.findProjectDir();

        findRootNode = new CheckBoxTreeNode();
        findTreeModel = new DefaultTreeModel(findRootNode);
        findDirTree.setModel(findTreeModel);
        FindUselessUtils.loopDirTrees(findRootNode, activeProject, ignoreDirs);
    }


    private ListCellRenderer createListOutputInfoRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                OutPutInfo outPutInfo = (OutPutInfo) value;
                setText(outPutInfo.getContent());
                return c;
            }
        };
    }


    private void loopProjectDir() {
        {

            List<VirtualFile> selectedDirs = findRootNode.getCheckParentsNode();
            for (VirtualFile virtualFile : selectedDirs) {
                for (VirtualFile childFile : virtualFile.getChildren()) {
                    if (childFile.getName().startsWith(".") || List.of(ignoreDirs).contains(childFile.getName()))
                        continue;
                    FindUselessUtils.loopVirtualDir(ignoreDirs, childFile, findResFiles, new FindUselessUtils.LoopCallback() {
                        @Override
                        public void callbackUsefulFile(ResFileInfo f, OutPutInfo outPutInfo) {
                            if (f != null)
                                usefulFiles.add(f);
                        }
                    });
                }
            }


            FindUselessUtils.loopTextFiles(resTextFiles, findResFiles, new FindUselessUtils.LoopCallback() {
                @Override
                public void callbackUsefulFile(ResFileInfo f, OutPutInfo outPutInfo) {
                    if (f != null)
                        usefulFiles.add(f);
                }
            });

            infoOutPutList.addElement(new OutPutInfo("*****************????????????*****************"));
            infoOutPutList.addElement(new OutPutInfo("*****************????????????????????????????????????*****************"));

            findResFiles.removeAll(usefulFiles);
            long fileSize = 0;
            for (ResFileInfo file : findResFiles) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("?????????[")
                        .append(file.getResFile().getPath())
                        .append("]")
                        .append("????????????")
                        .append(" ???????????????:")
                        .append(FileUtils.formatFileSize(file.getResFile().length()))
                        .append("???");
                infoOutPutList.addElement(new OutPutInfo(stringBuilder.toString(), file.getResFile()));
                fileSize += file.getResFile().length();
            }

            StringBuilder info = new StringBuilder();
            info.append("????????????????????????????????????:");
            info.append(findResFiles.size());
            info.append("???");
            info.append("???????????????:");
            info.append(FileUtils.formatFileSize(fileSize));
            info.append("???");
            infoOutPutList.addElement(new OutPutInfo(info.toString()));
            deleteAll.setVisible(findResFiles != null && findResFiles.size() > 0);
        }
    }


    private void onOK() {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                // ?????????????????????
                List<VirtualFile> selectedDirs = rootNode.getCheckParentsNode();
                infoOutPutList.clear();
                ignoreDirs.clear();
                findResFiles.clear();
                usefulFiles.clear();
                resTextFiles.clear();
                // ????????????????????????
                for (VirtualFile dir : selectedDirs) {
                    ignoreDirs.add(dir.getPath());
                    findResFiles.addAll(FindUselessUtils.findFileRes(new File(dir.getPath()), new FindUselessUtils.LoopFileCallback() {
                        @Override
                        public void callbackFindTextFile(File f) {
                            resTextFiles.add(f);
                        }
                    }));
                }
                // ????????????
                infoOutPutList.addElement(new OutPutInfo(new StringBuilder().append("*************")
                        .append("??????????????????????????????")
                        .append("*************").toString()));
                long fileSize = 0;
                for (ResFileInfo file : findResFiles) {
                    fileSize += file.getResFile().length();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("[")
                            .append(file.getResFile().getPath())
                            .append("]")
                            .append("[????????????: ")
                            .append(FileUtils.formatFileSize(file.getResFile().length()))
                            .append("]");
                    infoOutPutList.addElement(new OutPutInfo(stringBuilder.toString()));
                }

                StringBuilder info = new StringBuilder();
                info.append("*************");
                info.append("?????????????????? ---->>>> ????????????:[")
                        .append("????????????:")
                        .append(findResFiles.size())
                        .append("] ")
                        .append("[")
                        .append("???????????????:")
                        .append(FileUtils.formatFileSize(fileSize))
                        .append("]");
                info.append("*************");
                infoOutPutList.addElement(new OutPutInfo(info.toString()));
                infoOutPutList.addElement(new OutPutInfo("*************"));
                infoOutPutList.addElement(new OutPutInfo("*************"));

                loopProjectDir();
            }
        });
    }


    private void onCancel() {
        dispose();
    }

}
