package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.menu.MenuProductResponse
import co.edu.iub.manaproject.service.MenuService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/menu")
class MenuController(
    private val menuService: MenuService
) {


    @GetMapping
    fun getMenu(
        @RequestParam(required = false) categoryId: Long?
    ): List<MenuProductResponse> {
        return if (categoryId == null) {
            menuService.getMenu()
        } else {
            menuService.getMenuByCategoryId(categoryId)
        }
    }
}