package com.dl.playfun.widget.dialog.version.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.dl.playfun.R;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.version.DownloadUtil;

import java.io.File;

/*
 *@Author 彭石林
 *@Description 更新APK提示弹窗
 *@Date 2020/9/29 22:27
 *@Phone 16620350375
 *@email 15616314565@163.com
 *Param
 *@return
 **/
public class UpdateDialogView {

    private static volatile UpdateDialogView INSTANCE;
    private Context context;
    private CancelOnclick cancelOnclick;
    /**
     * 文件存储路径
     */
    private static String paths = "";
    /**
     * 安卓弹出对话框
     */
    private Dialog dialog;
    /**
     * 自定义进度条
     */
    NumberProgressBar npb;
    /**
     * 进度条百分比
     */
    int progressNpb = 0;

    /**
     * UI主线程同步显示UI
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    npb.setProgress(progressNpb);
                    break;
            }
        }
    };

    public static UpdateDialogView getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MVDialog.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UpdateDialogView(context);
                }
            }
        } else {
            init(context);
        }
        return INSTANCE;
    }

    private UpdateDialogView(Context context) {
        this.context = context;
    }

    private static void init(Context context) {
        INSTANCE.context = context;
    }

    /**
     * @return
     * @Author 彭石林
     * @Description 弹出对话框提示用书是否更新程序
     * @Date 2020/9/26 21:58
     * @Phone 16620350375
     * @email 15616314565@163.com
     * Param [mActivity, title, update_info, apkUrl, apkName]
     **/
    public UpdateDialogView getUpdateDialogView(String title, String update_info, String apkUrl, boolean isUpdate, String apkName) {
        //初始化自定义对话框
        dialog = new Dialog(context, R.style.UpdateAppDialog);
        LinearLayout popView = (LinearLayout) LayoutInflater.
                from(context).inflate(R.layout.update_app_dialog, null);
        //安装路径默认采用应用程序file路径
        paths = context.getApplicationContext().getFilesDir().getAbsolutePath();
        npb = popView.findViewById(R.id.npb); //升级进度条
        //标题
        TextView tv_title = popView.findViewById(R.id.tv_title);
        tv_title.setText(title);
        //更新内容
        TextView tv_update_info = popView.findViewById(R.id.tv_update_info);
        tv_update_info.setText(update_info);
        Button btn_ok = popView.findViewById(R.id.btn_ok); //升级按钮
        ImageView iv_close = popView.findViewById(R.id.iv_close);//取消升级按钮
        LinearLayout ll_close = popView.findViewById(R.id.ll_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
                if (cancelOnclick != null)
                    cancelOnclick.cancel();
            }
        });
        if (isUpdate) {
            ll_close.setVisibility(View.GONE);
        }
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_close.setVisibility(View.GONE);
                btn_ok.setVisibility(View.GONE);
                npb.setVisibility(View.VISIBLE);
                npb.setMax(100);
                npb.setProgress(0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DownloadUtil.get().download(apkUrl, paths, apkName + ".apk", new DownloadUtil.OnDownloadListener() {
                            /**
                             * 下载成功之后的文件回调接口
                             */
                            @Override
                            public void onDownloadSuccess(File fileApk) {
                                hide();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //兼容高低手机系统版本效验。低版本直接跳往安装，高版本获取权限效验
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri uri = FileProvider.getUriForFile(context, "com.dl.playfun.widget.dialog.version.UpdateFileProvider", fileApk);
                                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                                } else {
                                    intent.setDataAndType(Uri.fromFile(fileApk), "application/vnd.android.package-archive");
                                }
                                try {
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            /**
                             * 下载进度回调
                             */
                            @Override
                            public void onDownloading(int progress) {
                                progressNpb = progress;
                                handler.sendEmptyMessage(0);
                            }

                            /**
                             * 下载异常信息回调
                             */
                            @Override
                            public void onDownloadFailed(Exception e) {
                                Log.e("下载失败：", e.getMessage());
                                hide();
                            }
                        });
                    }
                }).start();
            }
        });

        dialog.setContentView(popView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        dialog.setFeatureDrawableAlpha(Window.FEATURE_OPTIONS_PANEL, 0);

        return INSTANCE;
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.dismiss();
    }

    /**
     * 设置取消按钮点击
     *
     * @param cancelOnclick
     * @return
     */
    public UpdateDialogView setConfirmOnlick(UpdateDialogView.CancelOnclick cancelOnclick) {
        this.cancelOnclick = cancelOnclick;
        return INSTANCE;
    }

    public interface CancelOnclick {
        void cancel();
    }
}
