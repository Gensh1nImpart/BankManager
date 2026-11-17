package com.bankmanager.ui;

import javax.swing.AbstractButton;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * {@link SwingWorker} 的极简封装，保证所有数据库调用离开 EDT。
 *
 * <p>约定：
 * <ul>
 *     <li>{@code work} 在后台线程执行，禁止在其中触碰 Swing 组件；</li>
 *     <li>执行期间禁用触发按钮并显示加载文案，鼠标变为等待状态；</li>
 *     <li>成功后回到 EDT 调用 {@code onSuccess}；失败默认弹窗展示
 *         {@link com.bankmanager.service.ServiceException} 的中文 message，
 *         也可传入自定义 {@code onError} 做内联提示。</li>
 * </ul>
 */
public final class Async {

    private Async() {
    }

    /** 后台执行并回到 EDT 处理结果，失败弹统一错误框。 */
    public static <T> void run(Component owner, AbstractButton trigger, String busyText,
                               Callable<T> work, Consumer<T> onSuccess) {
        run(owner, trigger, busyText, work, onSuccess, error -> UiUtils.showError(owner, error));
    }

    /** 后台执行；失败交给 {@code onError} 处理（例如表单内联提示）。 */
    public static <T> void run(Component owner, AbstractButton trigger, String busyText,
                               Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        UiUtils.checkEdt();
        String originalText = trigger == null ? null : trigger.getText();
        if (trigger != null) {
            trigger.setEnabled(false);
            if (busyText != null) {
                trigger.setText(busyText);
            }
        }
        Cursor originalCursor = owner.getCursor();
        owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                if (trigger != null) {
                    trigger.setEnabled(true);
                    if (originalText != null) {
                        trigger.setText(originalText);
                    }
                }
                owner.setCursor(originalCursor);
                try {
                    T value = get();
                    if (onSuccess != null) {
                        onSuccess.accept(value);
                    }
                } catch (CancellationException ignored) {
                    // 主动取消，无需提示
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    if (onError != null) {
                        onError.accept(cause);
                    } else {
                        UiUtils.showError(owner, cause);
                    }
                }
            }
        }.execute();
    }

    /** 只关心成功、不需要返回值的高频操作（例如刷新）。 */
    public static <T> void load(Component owner, AbstractButton trigger, Callable<T> work,
                                Consumer<T> onSuccess) {
        run(owner, trigger, null, work, onSuccess);
    }

    /** 无返回值操作。 */
    public static void runVoid(Component owner, AbstractButton trigger, String busyText,
                               Runnable work, Runnable onSuccess) {
        run(owner, trigger, busyText, () -> {
            work.run();
            return null;
        }, ignored -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        });
    }
}
