package me.dingtou.strategy.price.futu;

import com.futu.openapi.*;
import com.futu.openapi.pb.*;
import com.google.protobuf.GeneratedMessageV3;
import me.dingtou.constant.Market;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * futu api
 *
 * @author yuanhongbo
 */
public class FuTuAPI implements FTSPI_Qot, FTSPI_Conn {
    private static final FTAPI_Conn_Qot QOT = new FTAPI_Conn_Qot();
    private static boolean INIT = false;

    private static final FuTuAPI INSTANCE = new FuTuAPI();

    protected Map<Integer, Req> apiReqMap = new ConcurrentHashMap<>();

    private static final String IP = "127.0.0.1";
    private static final short PORT = (short) 11111;

    static {
        FuTuAPI.QOT.setClientInfo("javaClient", 1);  //设置客户端信息
        FuTuAPI.QOT.setConnSpi(INSTANCE);  //设置连接回调
        FuTuAPI.QOT.setQotSpi(INSTANCE);   //设置交易回调
        FuTuAPI.QOT.initConnect(IP, PORT, false);
    }

    /**
     * @return
     */
    public static FuTuAPI getInstance() {
        if (!INIT) {
            synchronized (FuTuAPI.class) {
                while (!INIT) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 订阅股票
     *
     * @param market
     * @param code
     * @return
     */
    private boolean syncSub(Market market, String code) {
        int fuTuMarket = convert(market);
        Object syncEvent = new Object();
        synchronized (syncEvent) {
            QotCommon.Security sec = QotCommon.Security.newBuilder()
                    .setMarket(fuTuMarket)
                    .setCode(code)
                    .build();
            QotSub.C2S c2s = QotSub.C2S.newBuilder()
                    .addSecurityList(sec)
                    .addSubTypeList(QotCommon.SubType.SubType_Basic_VALUE)
                    .addSubTypeList(QotCommon.SubType.SubType_KL_Day_VALUE)
                    .setIsSubOrUnSub(true)
                    .build();
            QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
            int seqNo = FuTuAPI.QOT.sub(req);
            if (seqNo == 0) {
                // 重试建立连接
                FuTuAPI.QOT.initConnect(IP, PORT, false);
                throw new RuntimeException("sub error");
            }
            FuTuAPI.Req apiReq = new Req(seqNo, ProtoID.QOT_SUB, syncEvent);
            apiReqMap.put(seqNo, apiReq);
            try {
                syncEvent.wait(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException("sub timeout");
            }
            return ((QotSub.Response) apiReq.rsp).getRetType() == Common.RetType.RetType_Succeed_VALUE;
        }
    }


    /**
     * 获取价格
     *
     * @param market
     * @param code
     * @return
     */
    public double syncGetBasicQot(Market market, String code) {
        syncSub(market, code);
        int fuTuMarket = convert(market);
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            QotCommon.Security sec = QotCommon.Security.newBuilder()
                    .setMarket(fuTuMarket)
                    .setCode(code)
                    .build();
            QotGetBasicQot.C2S c2s = QotGetBasicQot.C2S.newBuilder()
                    .addSecurityList(sec)
                    .build();
            QotGetBasicQot.Request req = QotGetBasicQot.Request.newBuilder().setC2S(c2s).build();
            int seqNo = FuTuAPI.QOT.getBasicQot(req);
            if (seqNo == 0) {
                throw new RuntimeException("getBasicQot error");
            }
            FuTuAPI.Req apiReq = new Req(seqNo, ProtoID.QOT_GETBASICQOT, syncEvent);
            apiReqMap.put(seqNo, apiReq);
            try {
                syncEvent.wait(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException("getBasicQot timeout");
            }
            QotGetBasicQot.Response qotGetBasicQotResp = (QotGetBasicQot.Response) apiReq.rsp;
            if (qotGetBasicQotResp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                throw new RuntimeException("getBasicQot result error");
            }
            return qotGetBasicQotResp.getS2C().getBasicQotList(0).getCurPrice();
        }
    }


    /**
     * 获取价格K线
     *
     * @param market
     * @param code
     * @param num
     * @return
     */
    public List<QotCommon.KLine> syncGetKL(Market market, String code, int num) {
        syncSub(market, code);
        int fuTuMarket = convert(market);
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            QotCommon.Security sec = QotCommon.Security.newBuilder()
                    .setMarket(fuTuMarket)
                    .setCode(code)
                    .build();
            QotGetKL.C2S c2s = QotGetKL.C2S.newBuilder()
                    .setSecurity(sec)
                    .setKlType(QotCommon.KLType.KLType_Day_VALUE)
                    .setRehabType(QotCommon.RehabType.RehabType_Forward_VALUE)
                    .setReqNum(num)
                    .build();
            QotGetKL.Request req = QotGetKL.Request.newBuilder().setC2S(c2s).build();
            int seqNo = FuTuAPI.QOT.getKL(req);
            if (seqNo == 0) {
                throw new RuntimeException("getKL error");
            }
            FuTuAPI.Req apiReq = new Req(seqNo, ProtoID.QOT_GETKL, syncEvent);
            apiReqMap.put(seqNo, apiReq);
            try {
                syncEvent.wait(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException("getKL timeout");
            }
            QotGetKL.Response qotGetKLResp = (QotGetKL.Response) apiReq.rsp;
            if (qotGetKLResp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                throw new RuntimeException("getKL result error");
            }
            return qotGetKLResp.getS2C().getKlListList();
        }
    }

    /**
     * 转换市场
     *
     * @param market
     * @return
     */
    private int convert(Market market) {
        switch (market) {
            case SH:
                return QotCommon.QotMarket.QotMarket_CNSH_Security_VALUE;
            case SZ:
                return QotCommon.QotMarket.QotMarket_CNSZ_Security_VALUE;
            case HK:
                return QotCommon.QotMarket.QotMarket_HK_Security_VALUE;
            default:
                throw new IllegalArgumentException("不支持的市场类型");
        }
    }


    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
        FuTuAPI.INIT = true;
        System.out.printf("Qot onInitConnect: ret=%b desc=%s connID=%d\n", errCode, desc, client.getConnectID());
    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        System.out.printf("Qot onDisconnect: ret=%b  connID=%d\n", errCode, client.getConnectID());
        FuTuAPI.QOT.initConnect(IP, PORT, false);
    }

    @Override
    public void onReply_Sub(FTAPI_Conn client, int nSerialNo, QotSub.Response rsp) {
        handleQotOnReply(nSerialNo, ProtoID.QOT_SUB, rsp);
    }

    @Override
    public void onReply_GetBasicQot(FTAPI_Conn client, int nSerialNo, QotGetBasicQot.Response rsp) {
        handleQotOnReply(nSerialNo, ProtoID.QOT_GETBASICQOT, rsp);
    }

    @Override
    public void onReply_GetKL(FTAPI_Conn client, int nSerialNo, QotGetKL.Response rsp) {
        handleQotOnReply(nSerialNo, ProtoID.QOT_GETKL, rsp);
    }

    /**
     * 统一处理返回
     *
     * @param serialNo
     * @param protoID
     * @param rsp
     */
    void handleQotOnReply(int serialNo, int protoID, GeneratedMessageV3 rsp) {
        Req req = getReq(serialNo, protoID);
        if (req != null) {
            synchronized (req.syncEvent) {
                req.rsp = rsp;
                req.syncEvent.notifyAll();
            }
        }
    }

    /**
     * 获取请求上下文
     *
     * @param serialNo
     * @param protoID
     * @return
     */
    Req getReq(int serialNo, int protoID) {
        Req req = apiReqMap.getOrDefault(serialNo, null);
        if (req != null && req.protoID == protoID) {
            apiReqMap.remove(serialNo);
            return req;
        }
        return null;
    }

    /**
     * 请求上下文
     */
    static class Req {
        int seqNo;
        int protoID;
        Object syncEvent;
        GeneratedMessageV3 rsp;

        Req(int seqNo, int protoID, Object syncEvent) {
            this.seqNo = seqNo;
            this.protoID = protoID;
            this.syncEvent = syncEvent;
        }
    }
}