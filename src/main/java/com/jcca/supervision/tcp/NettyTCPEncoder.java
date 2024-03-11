package com.jcca.supervision.tcp;

import com.jcca.common.LogUtil;
import com.jcca.supervision.data.frame.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author sophia
 * @description 编码
 * @date 2023/11/29 9:08
 */
public class NettyTCPEncoder extends MessageToByteEncoder<BaseDataFrame> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, BaseDataFrame dataFrame, ByteBuf out) throws Exception {


        if (dataFrame instanceof LoginDataFrame) {    //登录
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());
            out.writeCharSequence(LoginDataFrame.getUserName(), Charset.forName("GBK"));
            out.writeCharSequence(LoginDataFrame.getPassword(), Charset.forName("GBK"));

        } else if (dataFrame instanceof HeartbeatFrame) {   //心跳
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());

        } else if (dataFrame instanceof GetNodesFrame) {   //子孙节点
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());
            GetNodesFrame nodes = (GetNodesFrame) dataFrame;
            out.writeInt(Integer.decode(nodes.getRootId()));

        } else if (dataFrame instanceof GetSubstructFrame) {    //子节点
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());
            GetSubstructFrame node = (GetSubstructFrame) dataFrame;
            out.writeInt(Integer.decode(node.getRootId()));

        } else if (dataFrame instanceof GetPrpertyFrame) {    //获取属性点
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());
            out.writeInt(1);
            GetPrpertyFrame node = (GetPrpertyFrame) dataFrame;
            out.writeInt(Integer.decode(node.getId()));

        }else if (dataFrame instanceof SetAlarmModeFrame) {    //设置告警模式
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());
            SetAlarmModeFrame alarm = (SetAlarmModeFrame) dataFrame;
            out.writeInt(Integer.decode(alarm.getGroupId()));
            out.writeInt(Integer.decode(alarm.getMode()));
            out.writeInt(Integer.decode(alarm.getCount()));
            out.writeInt(Integer.decode(alarm.getIds()));

        } else if (dataFrame instanceof SetDynAccessModeFrame) {  //设置数据模式
            SetDynAccessModeFrame mode = (SetDynAccessModeFrame) dataFrame;
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());
            out.writeInt(dataFrame.getNum());
            out.writeInt(1);
            out.writeInt(mode.getSeconds());
            out.writeInt(1);
            out.writeInt(Integer.decode(mode.getIds()));

        } else if (dataFrame instanceof GetActiveAlarmFrame) {   //当前告警
            out.writeInt(dataFrame.getLen());
            out.writeInt(dataFrame.getNum());
            out.writeInt(dataFrame.getType());
        }else {
            logger.warn(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "不能未知类型消息内容", ByteBufUtil.hexDump(out)));
        }
    }


    /**
     * 登录
     */
    private void encode(LoginDataFrame frame, ByteBuf out) {

    }

}
