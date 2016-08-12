package com.huaqin.android.market.sdk;

import java.io.IOException;

import org.apache.http.HttpException;

import com.huaqin.android.market.sdk.bean.SortList;
import com.huaqin.android.market.sdk.rest.BaseResource;

/**
 * 新版分类信息获取
 * @author zhouyong
 *
 */
public class SortProvider extends BaseResource{
    
    /**
     * 新版根据分类ID获取下级分类列表
     * @param cateId
     *          分类ID，根ID为0
     * @return
     */
    public static SortList getSortsList(int sortId) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/sorts/"+sortId,SortList.class);
    }

}
