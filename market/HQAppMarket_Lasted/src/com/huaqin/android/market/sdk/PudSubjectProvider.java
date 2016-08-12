package com.huaqin.android.market.sdk;

import java.io.IOException;


import org.apache.http.HttpException;

import com.huaqin.android.market.sdk.bean.ApplicationList;
import com.huaqin.android.market.sdk.bean.NewSubject;
import com.huaqin.android.market.sdk.bean.NewSubjectList;
import com.huaqin.android.market.sdk.rest.BaseResource;

/**
 * 布丁控专栏推荐信息获取
 * @author zhouyong
 *
 */
public class PudSubjectProvider extends BaseResource{
    
    /**
     * 获取布丁控专栏推荐列表
     * @param page
     *          页码
     * @return
     *          布丁控专栏主题（精简的）列表
     */
    public static NewSubjectList getPudSubjectList(int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/pudsubjects/"+page,NewSubjectList.class);
    }
    
    /**
     * 获取单个布丁控专栏主题详细信息
     * @param pSubjId
     *          主题ID
     * @return
     *     布丁控 主题详细信息
     */
    public static NewSubject getPudSubjectDetail(int pSubjId) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/pudsubject/"+pSubjId,NewSubject.class);
    }
    
    /**
     * 获取某个布丁控主题下的推荐列表
     * @param pSubjId
     *          主题ID
     * @param page
     *          页码
     * @return
     *          话题列表
     */
   /* public static PudTopicList getPudTopicListOfSubject(int pSubjId,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/pudsubject/"+pSubjId+"/pTopics/"+page,PudTopicList.class);
    }*/
    public static ApplicationList getPudTopicListOfSubject(int pSubjId,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/pudsubject/"+pSubjId+"/pTopics/"+page,ApplicationList.class);
    }

}
