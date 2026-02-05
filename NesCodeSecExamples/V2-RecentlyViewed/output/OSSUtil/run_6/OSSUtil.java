<|editable_region_start|>
package com.cubic.api.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.PutObjectRequest;

/**
 * OSS工具类
 *
 * @author fei.yu
 * @date 2018/07/18
 */
public class OSSUtil {
	private final static Logger log = LoggerFactory.getLogger(OSSUtil.class);
	private static String endpoint = "http://oss-cn-qingdao.aliyuncs.com";
	private static String accessKeyId = "JnnEksdsd2P28jwZ";
	private static String accessKeySecret = "your_access_key_secret";
	private static String bucketName = "cubic-vanke";
	private static String img_suffix = ".jpg";
	private static String head = "https://cubic-vanke.oss-cn-qingdao.aliyuncs.com/";
	
	public static String uploadOSSToInputStream(String base64,String key) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String ret = "";
        InputStream in = null;
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                System.out.println("Creating bucket " + bucketName + "\n");
                ossClient.createBucket(bucketName);
                CreateBucketRequest createBucketRequest= new CreateBucketRequest(bucketName);
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
                ossClient.createBucket(createBucketRequest);
            }
            in = new ByteArrayInputStream(Util.decryptBASE64(base64));
            String file_name = fileName();
            ossClient.putObject(new PutObjectRequest(bucketName,key+"/"+file_name, in));
            ret = head+key+"/"+file_name;
        } catch (Exception e) {
        	log.error("上传oss失败，原因："+e.getMessage());
        }  finally {
            ossClient.shutdown();
            try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				log.error("关闭流出错："+e.getMessage());
			}
        }
        return ret;
    }
    
	private static String fileName() {
		return Util.createUuid()+img_suffix;
	}
}
<|editable_region_end|>
```
