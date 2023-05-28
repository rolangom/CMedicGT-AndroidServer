package com.rolangom.cmedicgt.domains

import kotlinx.coroutines.flow.Flow

interface ChildrenRepoProvider<T, F: Filterable> {
    fun getChildRepo(parentId: String): Flow<BaseRepo<T, F>>
}

interface BaseRepoWithChildren<T, F: Filterable, C, FC: Filterable> : BaseRepo<T, F>, ChildrenRepoProvider<C, FC> {

}