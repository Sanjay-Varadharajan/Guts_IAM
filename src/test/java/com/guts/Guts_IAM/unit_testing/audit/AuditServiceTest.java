package com.guts.Guts_IAM.unit_testing.audit;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.auditlog.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditRepository auditRepo;

    @InjectMocks
    private AuditService auditService;

    @Test
    void testSaveAudit_shouldSaveSuccessfully() {

        AuditLog auditLog = new AuditLog();

        auditService.saveAudit(auditLog);


        verify(auditRepo, times(1)).save(auditLog);
    }
}