package org.cocos2dx.lua;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSON;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;

import java.util.List;

public class ChessWeChat {

    public static int INVALID_LUAFUNC = -1;

    private static IWXAPI api = null; // 相应的包，请集成SDK后自行引入

    private static int mLuaFunc = INVALID_LUAFUNC;

    public static class LoginResult {
        public static int LOGIN_OK = 0;
        public static int LOGIN_CANCEL = 1;
        public static int LOGIN_FAILED = 2;

        private int mCode;
        private String mMessage;
        private String mData;

        public void setCode(int code) {
            mCode = code;
        }

        public void setMessage(String message) {
            mMessage = message;
        }

        public void setData(String data) {
            mData = data;
        }

        public String serialize() {
            return JSON.toJSONString(this);
        }
    }

    public static IWXAPI getApi(Context context) {
        if (api == null)
            api = WXAPIFactory.createWXAPI(context, ChessConfig.WECHAT_APPID);
        return api;
    }

    public static void callLuaFunc(LoginResult result) {
        if (result != null && mLuaFunc > INVALID_LUAFUNC) {
            Cocos2dxLuaJavaBridge.callLuaFunctionWithString(mLuaFunc, result.serialize());
        }
    }

    /**
     * 判断微信客户端是否存在
     *
     * @return true安装, false未安装
     */
    public static boolean isWeChatAppInstalled(Context context) {
        if(getApi(context).isWXAppInstalled()) {
            return true;
        } else {
            final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName;
                    if (pn.equalsIgnoreCase("com.tencent.mm")) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     *  微信登录
     * @param context 上下文
     * @param luaFunc lua回调
     * */
    public static void sendLogin(Context context, int luaFunc) {
        mLuaFunc = luaFunc;
        if (!isWeChatAppInstalled(context)) {
            LoginResult result = new LoginResult();
            result.setCode(LoginResult.LOGIN_FAILED);
            result.setMessage("未安装微信");
            callLuaFunc(result);
            return;
        }
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "diandi_wx_login";
        getApi(context).sendReq(req);
    }
}
