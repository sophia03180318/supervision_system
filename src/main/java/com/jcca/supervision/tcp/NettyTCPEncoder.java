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

            out.writeByte(dataFrame.getLen());
            out.writeByte(dataFrame.getNum());
            out.writeByte(dataFrame.getType());

        if (dataFrame instanceof LoginDataFrame) {    //登录

            out.writeCharSequence(LoginDataFrame.getUserName(), Charset.forName("GBK"));
            out.writeCharSequence(LoginDataFrame.getPassword(), Charset.forName("GBK"));

        } else if (dataFrame instanceof HeartbeatFrame) {   //心跳


        } else if (dataFrame instanceof GetNodesFrame) {   //子孙节点
            GetNodesFrame nodes = (GetNodesFrame) dataFrame;
            out.writeLongLE(Long.decode(nodes.getRootId()));

        } else if (dataFrame instanceof GetSubstructFrame) {    //子节点
            GetSubstructFrame node = (GetSubstructFrame) dataFrame;
            out.writeLongLE(Long.decode(node.getRootId()));

        } else if (dataFrame instanceof SetAlarmModeFrame) {    //设置告警模式
            SetAlarmModeFrame alarm = (SetAlarmModeFrame) dataFrame;
            out.writeLongLE(Long.decode(alarm.getGroupId()));
            out.writeLongLE(Long.decode(alarm.getMode()));
            out.writeLongLE(Long.decode(alarm.getCount()));
            out.writeLongLE(Long.decode(alarm.getIds()));

        } else if (dataFrame instanceof SetDynAccessModeFrame) {  //设置数据模式
            SetDynAccessModeFrame model = (SetDynAccessModeFrame) dataFrame;

        } else {
            logger.warn(LogUtil.buildLog(ctx.channel().remoteAddress().toString(), "不能未知类型消息内容", ByteBufUtil.hexDump(out)));
        }
    }


    /**
     * 登录
     */
    private void encode(LoginDataFrame frame, ByteBuf out) {

    }

}
