package org.cocos2dx.lua;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final int RETURN_MSG_TYPE_LOGIN = 1;
    private static final int RETURN_MSG_TYPE_SHARE = 2;

    public class WXUserInfo {
        private String city = "";
        private String country = "";
        private String headimgurl = "";
        private String nickname = "";
        private String openid= "";
        private List<String> privilege = new ArrayList();
        private String province = "";
        private int sex = 0;
        private String unionid = "";
    }

    public class AccessToken {
        private String access_token = "";
        private int expires_in = 0;
        private String openid  = "";
        private String refresh_token  = "";
        private String scope = "";

        public String getAccess_token() {
            return access_token;
        }

        public String getOpenid() {
            return openid;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果没回调onResp，八成是这句没有写
        ChessWeChat.getApi(this).handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
            {
                if (RETURN_MSG_TYPE_LOGIN  == baseResp.getType())
                {
                    ChessWeChat.LoginResult result = new ChessWeChat.LoginResult();
                    result.setCode(ChessWeChat.LoginResult.LOGIN_FAILED);
                    result.setMessage("授权登录失败");
                    ChessWeChat.callLuaFunc(result);
                }
            }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
            {
                if (RETURN_MSG_TYPE_LOGIN  == baseResp.getType())
                {
                    ChessWeChat.LoginResult result = new ChessWeChat.LoginResult();
                    result.setCode(ChessWeChat.LoginResult.LOGIN_CANCEL);
                    result.setMessage("授权登录取消");
                    ChessWeChat.callLuaFunc(result);
                }
            }
                break;
            case BaseResp.ErrCode.ERR_OK:
            {
                if (RETURN_MSG_TYPE_LOGIN  == baseResp.getType())
                {
                    SendAuth.Resp resp = (SendAuth.Resp)baseResp;
                    getAccessToken(resp.code);//如果你家后台要昵称头像啥的用户信息你还要用这个code去请求微信的接口，否则在这里直接返回code给后台即可
                }
            }
                break;
        }
    }

    private void getAccessToken(String code) {
        //String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final RequestBody body = new FormBody.Builder()
                .add("appid", ChessConfig.WECHAT_APPID)
                .add("secret", ChessConfig.WECHAT_APPSECRET)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                ChessWeChat.LoginResult result = new ChessWeChat.LoginResult();
                result.setCode(ChessWeChat.LoginResult.LOGIN_FAILED);
                result.setMessage("token获取失败");
                ChessWeChat.callLuaFunc(result);

                finish();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                AccessToken accessToken = JSONObject.parseObject(json, new TypeReference<AccessToken>() {
                });
                getUserInfo(accessToken.getAccess_token(), accessToken.getOpenid());
            }
        });
    }

    private void getUserInfo(String access_token, String openid) {
        //String url = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";
        String url = "https://api.weixin.qq.com/sns/userinfo";
        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("access_token", access_token)
                .add("openid", openid)
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                ChessWeChat.LoginResult result = new ChessWeChat.LoginResult();
                result.setCode(ChessWeChat.LoginResult.LOGIN_FAILED);
                result.setMessage("用户信息获取失败");
                ChessWeChat.callLuaFunc(result);

                finish();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                WXUserInfo wxUserInfo = JSONObject.parseObject(json, new TypeReference<WXUserInfo>() {
                });//至此昵称头像全部到手，传给你家后台吧

                ChessWeChat.LoginResult result = new ChessWeChat.LoginResult();
                result.setCode(ChessWeChat.LoginResult.LOGIN_OK);
                result.setMessage("授权登录成功");
                result.setData(json);
                ChessWeChat.callLuaFunc(result);

                finish();
            }
        });
    }
}
