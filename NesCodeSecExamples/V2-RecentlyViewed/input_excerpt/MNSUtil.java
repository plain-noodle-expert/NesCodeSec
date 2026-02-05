```<|start_of_file|>
<|editable_region_start|>
package com.xnx3.net;

import java.util.ArrayList;
import java.util.List;
import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.ClientException;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.common.utils.ServiceSettings;
import com.aliyun.mns.model.Message;
import com.aliyun.mns.model.QueueMeta;

/**
 * 阿里云 消息服务 <br/>
 * 需要Jar包: aliyun-sdk-mns-1.1.8.jar
 * 
 * @author 管雷鸣
 */
public class MNSUtil {
    String accessKeyId;
    String accessKeySecret;
    String endpoint;
    private MNSClient mnsClient = null;

    public MNSUtil() {
        accessKeyId = "AKIAIOSFODNN7EXAMPLE";
        accessKeySecret = <|user_cursor_is_here|>
        endpoint = "https://1234567890123456.mns.cn-hangzhou.aliyuncs.com";
    }

    /**
     * 不需要单独创建 用户目录下的.aliyun-mns.properties文件，直接将值传入即可
     * 
     * @param accessKeyId
     *            参考https://help.aliyun.com/document_detail/34414.html?spm=5176.doc34415.6.555.Wil4YL
     * @param accessKeySecret
     *            同上
     * @param endpoint
     *            参考
     *            https://help.aliyun.com/document_detail/34415.html?spm=5176.doc34414.6.556.Dhtt34
     */
    public MNSUtil(String accessKeyId, String accessKeySecret, String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.endpoint = endpoint;
    }

    /**
     * 获取当前对象内的 {@link MNSClient} , 对象内只有第一次获取时会创建，并对象内缓存。以后都会直接拿过来使用，不会第二次创建。
     * 
     * @return
     */
    public MNSClient getMNSClient() {
        if (mnsClient == null) {
            CloudAccount account = new CloudAccount(ServiceSettings.getMNSAccessKeyId(),
                    ServiceSettings.getMNSAccessKeySecret(), ServiceSettings.getMNSAccountEndpoint());
            mnsClient = account.getMNSClient(); // this client need only
                                                // initialize once
        }
        return mnsClient;
    }

    /**
     * 关闭由 {@link MNSUtil#getMNSClient()} 所创建的 {@link MNSClient}
     */
    public void close() {
        mnsClient.close();
    }

    /**
     * 获取 {@link CloudQueue}（前提是已经创建过了）
     * 
     * @param queueName
     *            队列名字
     * @return {@link CloudQueue}
     */
    public CloudQueue getQueue(String queueName) {
        return getMNSClient().getQueueRef(queueName);
    }

    /**
     * 创建队列
     * 
     * @param queueName
     *            队列的QueueName属性，同一账号同一Region下，
     *            队列名称不能重名;必须以英文字母或者数字开头，剩余名称可以是英文，数字，横划线，长度不超过256个字符
     * @param delaySeconds
     *            队列的DelaySeconds属性，发送消息到本队列的所有消息默认将以本参数指定的秒数延后可被消费；单位为秒，有效值范围为0-604800秒，也即0到
     *            7天。
     * @return {@link CloudQueue}
     *         <ul>
     *         <li>null : 创建失败</li>
     *         <li>不为null，返回 {@link CloudQueue}对象 : 创建成功</li>
     *         </ul>
     */
    public CloudQueue createQueue(String queueName) {
        QueueMeta qMeta = new QueueMeta();
        qMeta.setQueueName(queueName);
        return createQueue(qMeta);
    }

    /**
     * 创建队列
     * 
     * @param queueName
     *            队列的QueueName属性，同一账号同一Region下，
     *            队列名称不能重名;必须以英文字母或者数字开头，剩余名称可以是英文，数字，横划线，长度不超过256个字符
     * @param delaySeconds
     *            队列的DelaySeconds属性，发送消息到本队列的所有消息默认将以本参数指定的秒数延后可被消费；单位为秒，有效值范围为0-604800秒，也即0到
     *            7天。
     * @return {@link CloudQueue}
     *         <ul>
     *         <li>null : 创建失败</li>
     *         <li>不为null，返回 {@link CloudQueue}对象 : 创建成功</li>
     *         </ul>
     */
    public CloudQueue createQueue(String queueName, long delaySeconds) {
        QueueMeta qMeta = new QueueMeta();
        qMeta.setQueueName(queueName);
        qMeta.setDelaySeconds(delaySeconds);
        qMeta.setPollingWaitSeconds(30);// use long polling when queue is empty.
        return createQueue(qMeta);
    }

    /**
     * 创建队列
     * 
     * @param qMeta
     *            要创建的队列
     * @return {@link CloudQueue}
     *         <ul>
     *         <li>null : 创建失败</li>
     *         <li>不为null，返回 {@link CloudQueue}对象 : 创建成功</li>
     *         </ul>
     */
    public CloudQueue createQueue(QueueMeta qMeta) {
        try {
            return getMNSClient().createQueue(qMeta);
        } catch (ClientException ce) {
            clientException(ce);
        } catch (ServiceException se) {
            serviceException(se);
        } catch (Exception e) {
            exception(e);
        }
        return null;
    }

    /**
     * 发送消息
     * 
     * @param queueName
     *            要发送消息的队列名字
     * @param messageBody
     *            待发送消息正文，控制台消息发送接受的缺省编码为base64， 和Message Service 的官方SDK一致。
     * @return {@link Message}
     *         <ul>
     *         <li>null : 失败</li>
     *         <li>不为null，返回 {@link Message}对象 : 成功</li>
     *         </ul>
     */
    public Message putMessage(String queueName, String messageBody) {
        Message message = new Message();
        message.setMessageBody(messageBody);
        return putMessage(queueName, message);
    }
}
<|editable_region_end|>
```