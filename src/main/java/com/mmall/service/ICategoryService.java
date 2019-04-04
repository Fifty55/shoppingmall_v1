package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * Created by yuchi on 4/2/19.
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);
    ServerResponse updateCategoryName(String categoryName, Integer parentId);
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);
    ServerResponse<List<Integer>> getCategoryAndChildrenById(Integer categoryId);
}
