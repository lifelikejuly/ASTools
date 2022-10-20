package com.vdian.wddevplugin.tools.dartjsonformat;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.vdian.wddevplugin.tools.dartjsonformat.model.MustacheEntity;
import com.vdian.wddevplugin.utils.ClsUtils;
import com.vdian.wddevplugin.utils.MustacheUtils;
import org.jetbrains.annotations.NotNull;


/**
 * @author shiki
 */
public class DartDataWriter extends WriteCommandAction.Simple {
    private Project project;
    private MustacheEntity mustacheEntity;
    private ClsUtils clsUtils;
    private MustacheUtils.GenDartFileListener genDartFileListener;
    private PsiFile file;

    public DartDataWriter(Project project, ClsUtils clsUtils, MustacheEntity mustacheEntity, PsiFile file) {
        super(project, file);
        this.project = project;
        this.mustacheEntity = mustacheEntity;
        this.clsUtils = clsUtils;

        this.file = file;
    }

    public void start() {
        // 异步任务
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "DartJsonFormat") {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    progressIndicator.setIndeterminate(true);
                    execute();
                    progressIndicator.setIndeterminate(false);
                    progressIndicator.setFraction(1.0);
                } catch (Exception e) {
                    progressIndicator.setIndeterminate(false);
                    progressIndicator.setFraction(1.0);
                }
            }
        });
    }

    @Override
    protected void run() throws Throwable {
        mustacheEntity.setDir(clsUtils.getClsDir(file));
        MustacheUtils.genDartFile(mustacheEntity, genDartFileListener);
        file.getParent().getVirtualFile().refresh(true, true);
    }

    public MustacheUtils.GenDartFileListener getGenDartFileListener() {
        return genDartFileListener;
    }

    public void setGenDartFileListener(MustacheUtils.GenDartFileListener genDartFileListener) {
        this.genDartFileListener = genDartFileListener;
    }
}
