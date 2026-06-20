package com.faisalaffan.javara.app.controller;

import com.faisalaffan.javara.app.security.RbacService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/t24/admin/rbac")
public class RbacController {

    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping("/users/{userId}/permissions")
    public Set<String> getUserPermissions(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        return rbacService.getUserPermissions(userId, tenantId != null ? tenantId : "default");
    }

    @PostMapping("/users/{userId}/roles/{roleCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignRole(
            @PathVariable UUID userId,
            @PathVariable String roleCode,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-Granted-By", required = false) String grantedBy) {
        rbacService.assignRole(userId, roleCode,
            tenantId != null ? tenantId : "default",
            grantedBy != null ? grantedBy : "system");
    }

    @DeleteMapping("/users/{userId}/roles/{roleCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeRole(
            @PathVariable UUID userId,
            @PathVariable String roleCode,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        rbacService.revokeRole(userId, roleCode, tenantId != null ? tenantId : "default");
    }
}
