package com.huaqin.android.market.sdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.huaqin.android.market.sdk.bean.Subject;
import com.huaqin.android.market.sdk.bean.SubjectList;
import com.huaqin.android.market.sdk.bean.TopicList;
import com.huaqin.android.market.sdk.rest.BaseResource;

/**
 * 专栏推荐信息获取
 * @author duanweiming
 *
 */
public class SubjectProvider extends BaseResource{
    
    /**
     * 获取专栏推荐列表
     * @param page
     *          页码
     * @return
     *          专栏主题（精简的）列表
     */
    public static SubjectList getSubjectList(int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/subjects/"+page,SubjectList.class);
    }
    
    /**
     * 获取单个专栏主题详细信息
     * @param subjId
     *          主题ID
     * @return
     *      主题详细信息
     */
    public static Subject getSubjectDetail(int subjId) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/subject/"+subjId,Subject.class);
    }
    
    /**
     * 获取某个主题下的话题列表
     * @param subjId
     *          主题ID
     * @param page
     *          页码
     * @return
     *          话题列表
     */
    public static TopicList getTopicListOfSubject(int subjId,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/subject/"+subjId+"/topics/"+page,TopicList.class);
    }
    
    /**
     * 某个主题下的顶，踩接口
     * @param subjId
     *          主题ID
     * @param up
     *          顶(up=1,down=0)
     * @param down
     *      踩(up=0,down=1)
     *         
     */
    
    public static void postSubjComment(String subjId,String up,String down) throws IOException, HttpException{	
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("subj_id", subjId));
        formParams.add(new BasicNameValuePair("subj_up", up));
        formParams.add(new BasicNameValuePair("subj_down", down));
        
	  post(ClientInfo.RESOURCE_ROOT_URL + "/subject/comment",formParams);
	}
    
    /**
     * 判断是否有最新专题
     * @return
     *   1:有，0：没有。
     */
    public static String getNewSubject() throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/subject/new",String.class);
    }
}
