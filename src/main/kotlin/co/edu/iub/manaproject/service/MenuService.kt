package co.edu.iub.manaproject.service

import co.edu.iub.manaproject.dto.menu.MenuProductResponse
import co.edu.iub.manaproject.repository.ProductRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class MenuService(
    private val productRepository: ProductRepository
) {

    @Transactional(readOnly = true)
    fun getMenu(): List<MenuProductResponse> {
        return productRepository
            .findAllByActiveTrueAndCategoryActiveTrueOrderByCategoryNameAscNameAsc()
            .map { product ->
                MenuProductResponse(
                    id = requireNotNull(product.id),
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    categoryId = requireNotNull(product.category?.id),
                    categoryName = requireNotNull(product.category?.name)
                )

            }
    }
    @Transactional(readOnly = true)
    fun getMenuByCategoryId(categoryId: Long): List<MenuProductResponse> {
        return productRepository
            .findAllByActiveTrueAndCategoryActiveTrueAndCategoryIdOrderByNameAsc(categoryId)
            .map { product ->
                MenuProductResponse(
                    id = requireNotNull(product.id),
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    categoryId = requireNotNull(product.category?.id),
                    categoryName = requireNotNull(product.category?.name)
                )
            }
    }
}