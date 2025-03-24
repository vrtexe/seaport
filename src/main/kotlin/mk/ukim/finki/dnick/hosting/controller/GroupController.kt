package mk.ukim.finki.dnick.hosting.controller

import jakarta.validation.Valid
import mk.ukim.finki.dnick.hosting.generated.model.*
import mk.ukim.finki.dnick.hosting.model.entity.Application
import mk.ukim.finki.dnick.hosting.service.GroupService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/api/v2/groups")
class GroupController(private val groupService: GroupService) {


    @PostMapping
    fun createGroup(@RequestBody @Valid groupCreateRequest: GroupCreateRequest): ResponseEntity<Group> {
        return ResponseEntity.ok(groupService.createGroup(groupCreateRequest).toDto())
    }

    @DeleteMapping("/{id}")
    fun deleteGroup(@PathVariable id: Int): ResponseEntity<Unit> {
        groupService.deleteGroup(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getAllGroups(pageable: Pageable = Pageable.unpaged()): ResponseEntity<GroupsResponse> {
        return ResponseEntity.ok(
            groupService.getGroups(pageable).let {
                GroupsResponse(
                    data = it.content.map { app -> app.toDto() },
                    metadata = ResponseMetadata(pagination = it.toPagination())
                )
            }
        )
    }

    @PatchMapping("/{id}")
    fun updateGroup(
        @PathVariable id: Int,
        @RequestBody @Valid groupUpdateRequest: GroupUpdateRequest
    ): ResponseEntity<Group> {
        return ResponseEntity.ok(groupService.updateGroup(id, groupUpdateRequest).toDto())
    }

    fun Application.toDto() = Group(
        id = this.id!!,
        name = this.name,
    )
}