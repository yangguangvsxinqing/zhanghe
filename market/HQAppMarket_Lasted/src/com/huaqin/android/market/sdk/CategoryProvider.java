package com.huaqin.android.market.sdk;

import java.io.IOException;

import org.apache.http.HttpException;

import com.huaqin.android.market.sdk.bean.CategoryList;
import com.huaqin.android.market.sdk.rest.BaseResource;

/**
 * 应用分类信息获取
 * @author duanweiming
 *
 */
public class CategoryProvider extends BaseResource{
    
    /**
     * 根据分类ID获取下级分类列表
     * @param cateId
     *          分类ID，根ID为0
     * @return
     */
    public static CategoryList getCategoriesList(int cateId) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/categories/"+cateId,CategoryList.class);
    }

}
