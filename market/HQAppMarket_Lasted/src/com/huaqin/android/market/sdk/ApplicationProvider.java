package com.huaqin.android.market.sdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.bean.ApplicationList;
import com.huaqin.android.market.sdk.bean.BatchInstallComment;
import com.huaqin.android.market.sdk.bean.BatchInstallCommentList;
import com.huaqin.android.market.sdk.bean.Comment;
import com.huaqin.android.market.sdk.bean.CommentList;
import com.huaqin.android.market.sdk.bean.HotWordList;
import com.huaqin.android.market.sdk.bean.InstalledApp;
import com.huaqin.android.market.sdk.bean.appNameList;
import com.huaqin.android.market.sdk.rest.BaseResource;

/**
 * 应用信息获取
 * @author duanweiming
 *
 */
public class ApplicationProvider extends BaseResource{
    
    /**
     * 获取ID单个应用详细信息
     * @param appId
     * @return
     */
    public static Application getAppDetail(int appId) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/app/"+appId,Application.class);
    }
    /**
     * 获取Topic里面单个应用详细信息
     * @param appId
     * @return
     */
    public static Application getTopicAppDetail(int appId) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/app/topic/"+appId,Application.class);
    }
    
    /**
     * 添加应用评论
     * @param appId
     * @param comment
     * @return
     */
    public static Comment postComment(int appId,Comment comment) throws IOException,HttpException{
        return post(ClientInfo.RESOURCE_ROOT_URL+"/app/"+appId+"/comment",comment,Comment.class);
    }
    
    /**
     * 更新应用评论
     * @param appId
     * @param comment
     * @return
     */
    public static Comment putComment(int appId,Comment comment) throws IOException,HttpException{
        return put(ClientInfo.RESOURCE_ROOT_URL+"/app/"+appId+"/comment",comment,Comment.class);
    }
    
    /**
     * 获取应用评论列表
     * @param appId
     * @param page
     * @return
     */
    public static CommentList getComments(int appId,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/app/"+appId+"/comments/"+page,CommentList.class);
    }
    
    /**
     * 获取推荐应用列表
     * @param page
     * @return
     */
    public static ApplicationList getRecommendedList(int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/recommend/"+page,ApplicationList.class);
    }
    
    /**
     * 获取相关应用列表
     * @param appId
     * @param page
     * @return
     */
    public static ApplicationList getRelatedList(int appId,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/related/"+appId+"/"+page,ApplicationList.class);
    }
    
    /**
     * 获取根据该应用下载量排行附近的其他推荐列表
     * @param totalDownloads 下载总量
     * 
     * @return
     */
    public static ApplicationList getAroundList(int totalDownloads) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/around/"+totalDownloads,ApplicationList.class);
    }
    
    
    /**
     * 获取最新应用列表
     * @param page
     * @return
     */
    public static ApplicationList getNewShelvingList(int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/new_shelving/"+page,ApplicationList.class);
    }
    
    /**
     * 应用排行列表
     * @param page
     * @return
     */
    public static ApplicationList getSortAppList(int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/sortapp/"+page,ApplicationList.class);
    }
    
    /**
     * 游戏排行列表
     * @param page
     * @return
     */
    public static ApplicationList getSortGameList(int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/sortgame/"+page,ApplicationList.class);
    }
    
    /**
     * 按下载排行获取应用列表
     * @param rankType
     *          排行类型（周排行，月排行，总排行）
     * @param page
     * @return
     */
    public static ApplicationList getRankingList(int rankType,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/ranking/"+rankType+"/"+page,ApplicationList.class);
    }
    
    /**
     * 通用类别获取应用列表
     * 
     * @param type
     * 			分类名称0：装机精灵；1：新版
     * @param id
     *          type为0时候代表cateIdID；为1时候代表sortId
     * @param orderType
     *          排序类型（按下载，按评分，按日期）
     * @param page
     * @return
     */
    
    public static ApplicationList getTypeList(int type,int id,int orderType,int page) throws IOException,HttpException{
       
    	if(type==1){
    		return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/sort/"+id+"/"+orderType+"/"+page,ApplicationList.class);
    	}else{
    		return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/category/"+id+"/"+orderType+"/"+page,ApplicationList.class);
    	}
     }
    
    /**
     * 按类别获取应用列表
     * 已被getTypeList函数覆盖
     * @param cateId
     *          类别ID
     * @param orderType
     *          排序类型（按下载，按评分，按日期）
     * @param page
     * @return
     */
    public static ApplicationList getCategoricalList(int cateId,int orderType,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/category/"+cateId+"/"+orderType+"/"+page,ApplicationList.class);
    }
    
    /**
     * 按新版类别获取应用列表
     * 已被getTypeList函数覆盖
     * @param sortId
     *          类别ID
     * @param orderType
     *          排序类型（按下载，按评分，按日期）
     * @param page
     * @return
     */
    public static ApplicationList getSortList(int sortId,int orderType,int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/sort/"+sortId+"/"+orderType+"/"+page,ApplicationList.class);
    }
    
    
    /**
     * 检查已安装应用是否有更新(旧版)
     * @param installedApps
     *          已安装应用列表
     * @return
     */
    public static ApplicationList getUpdateList(InstalledApp[] installedApps) throws IOException,HttpException{
        return post(ClientInfo.RESOURCE_ROOT_URL+"/apps/update",installedApps,ApplicationList.class);
    }
    
    /**
     * 检查已安装应用是否有更新(新版3.1后)
     * @param installedApps
     *          已安装应用列表
     * @return
     */
    public static ApplicationList getUpdateListNew(InstalledApp[] installedApps) throws IOException,HttpException{
        return post(ClientInfo.RESOURCE_ROOT_URL+"/apps/updatenew",installedApps,ApplicationList.class);
    }
    
    /**
     * 获取热点搜索词
     * @return
     */
    public static HotWordList getSearchHotWords() throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/search/hot_words",HotWordList.class);
    }
    
    /**
     * 根据关键字搜索应用
     * @param keyWord
     *          关键词
     * @return
     */
    public static ApplicationList getSearchResultList(String keyWord,int page) throws IOException,HttpException{
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("key_word", keyWord));
        formParams.add(new BasicNameValuePair("page",page+""));
        return post(ClientInfo.RESOURCE_ROOT_URL+"/apps/search",formParams,ApplicationList.class);
    }
    /**
     * 获取所有appName,供客户端即时匹配所用
     * 
     * @return StringList
     */
    public static appNameList getAllAppNames() throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/appnames",appNameList.class);
    }
    
    
    /**
     * 获取一键安装应用列表
     * @param page
     * @return
     */
    public static ApplicationList getBatchInstallList() throws IOException,HttpException{
        //return ResourceHelper.getInstance().getResource(ClientInfo.RESOURCE_ROOT_URL+"/apps/batchinstall").get(ApplicationList.class);
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/batchinstall",ApplicationList.class);
        
    }
    
    /**
     * 添加一键安装评论
     * @param comment
     * @return
     */
    public static BatchInstallComment postBatchInstallComment(BatchInstallComment comment) throws IOException,HttpException{
        return post(ClientInfo.RESOURCE_ROOT_URL+"/apps/batchInstallComment",comment,BatchInstallComment.class);
    }
    
    /**
     * 更新一键安装评论
     * @param comment
     * @return
     */
    public static BatchInstallComment putBatchInstallComment(BatchInstallComment comment) throws IOException,HttpException{
        return put(ClientInfo.RESOURCE_ROOT_URL+"/apps/batchInstallComment",comment,BatchInstallComment.class);
    }
    
    /**
     * 获取一键安装评论列表
     * @param page
     * @return
     */
    public static BatchInstallCommentList getBatchInstallComments(int page) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/apps/batchInstallComments/"+page,BatchInstallCommentList.class);
    }
   /* *//**
     * 顶接口
     * @param appId
     * @return
     *//*
    public static void postAppTop(String userId,int appId) throws IOException, HttpException{	
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", String.valueOf(appId)));
	  post(ClientInfo.RESOURCE_ROOT_URL + "/app/top",formParams);
	}
    *//**
     * 踩接口
     * @param appId
     * @return
     *//*
    public static void postAppTread(String userId,int appId) throws IOException, HttpException{	
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", String.valueOf(appId)));
	  post(ClientInfo.RESOURCE_ROOT_URL + "/app/tread",formParams);
	}*/
}
