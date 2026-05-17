package com.pickeat.backend.pickeat.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.PickeatResult;
import com.pickeat.backend.pickeat.domain.repository.PickeatRecordRepository;
import com.pickeat.backend.pickeat.domain.repository.PickeatResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickeatResultService {

    private final PickeatRecordRepository pickeatRecordRepository;
    private final PickeatResultRepository pickeatResultRepository;

    public PickeatResultResponse getByPickeatCode(String pickeatCode) {
        PickeatRecord pickeatRecord = getPickeatRecordByCode(pickeatCode);
        PickeatResult result = getPickeatResultByCode(pickeatRecord.getId());
        return PickeatResultResponse.of(result);
    }

    private PickeatRecord getPickeatRecordByCode(String pickeatCode) {
        return pickeatRecordRepository.findByCode(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.PICKEAT_RECORD_NOT_FOUND));
    }

    private PickeatResult getPickeatResultByCode(Long recordId) {
        return pickeatResultRepository.findByPickeatRecordId(recordId)
                .orElseThrow(() -> new ClientException(ClientErrorCode.PICKEAT_RESULT_NOT_FOUND));
    }
}
