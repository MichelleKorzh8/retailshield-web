package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.model.AssessmentResult;

/**
 * Коэффициент убыточности (Loss Ratio, Блок 3, раздел 9 — правка 11).
 *
 * Базовая формула:
 *     LossRatio = ожидаемые выплаты / премия
 *
 * В качестве оценки ожидаемых выплат используется ALE с учётом каскадного
 * эффекта (ALE_cascade). Получаемое отношение показывает, какую долю
 * собранной премии страховщик ожидает выплатить по этому клиенту.
 *
 * Интерпретация:
 *  < 0.50 — выгодный клиент, маржинальный портфельный сегмент;
 *  0.50–0.80 — нормальный коридор для устойчивого портфеля;
 *  > 0.80 — клиент с риском убыточности, требуется пересмотр условий.
 */
@Service
public class LossRatioService {

    public void calculate(AssessmentResult result) {
        if (result.getAnnualPremium() <= 0.0 || Double.isNaN(result.getAnnualPremium())) {
            result.setLossRatio(0.0);
            return;
        }
        double expectedLosses = result.getAleCascade();
        result.setLossRatio(expectedLosses / result.getAnnualPremium());
    }
}