package com.pickeat.backend.pickeat.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponseV2;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.PickeatResultV2;
import com.pickeat.backend.pickeat.domain.repository.PickeatRecordRepository;
import com.pickeat.backend.pickeat.domain.repository.PickeatResultRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickeatResultServiceV2 {

    private final PickeatRecordRepository pickeatRecordRepository;
    private final PickeatResultRepositoryV2 pickeatResultRepository;

    public PickeatResultResponseV2 getByPickeatCode(String pickeatCode) {
        PickeatRecord pickeatRecord = getPickeatRecordByCode(pickeatCode);
        PickeatResultV2 result = getPickeatResultByCode(pickeatRecord.getId());
        return PickeatResultResponseV2.of(result);
    }

    private PickeatRecord getPickeatRecordByCode(String pickeatCode) {
        return pickeatRecordRepository.findByCode(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PICKEAT_RECORD_NOT_FOUND));
    }

    private PickeatResultV2 getPickeatResultByCode(Long recordId) {
        return pickeatResultRepository.findByPickeatRecordId(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PICKEAT_RESULT_NOT_FOUND));
    }
}
