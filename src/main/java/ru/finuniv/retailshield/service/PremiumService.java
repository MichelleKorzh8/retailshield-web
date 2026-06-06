package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.data.Coefficients;
import ru.finuniv.retailshield.model.AssessmentResult;
import ru.finuniv.retailshield.model.Company;
import ru.finuniv.retailshield.model.PackageType;

/**
 * Расчёт годовой страховой премии по формуле Блока 3, раздел 6
 * (с учётом правки 10).
 *
 * P = S × T × K_отрасли × K_скора × K_размера × K_истории × K_сервиса × K_внеш × K_аудита
 *
 * Где:
 *  S          — страховая сумма,
 *  T          — базовая тарифная ставка пакета (1.0%/1.2%/1.5%/1.8%),
 *  K_отрасли  — отраслевой коэффициент (1.25 для всех типов ритейла),
 *  K_скора    — коэффициент класса риска (0.70/1.00/1.30/1.60),
 *  K_размера  — коэффициент размера бизнеса (0.8–1.4),
 *  K_истории  — коэффициент истории убытков (0.9–1.3),
 *  K_сервиса  — 0.85 при непрерывном мониторинге, 0.90 для Лайт + комплаенс-аудит,
 *  K_внеш     — коэффициент внешней экспозиции по результатам OSINT (1.0/1.5/2.0),
 *  K_аудита   — 0.90 при наличии действующего внешнего аудита, 1.00 без него.
 */
@Service
public class PremiumService {

    public void calculate(AssessmentResult result, Company company) {

        // Класс Critical — отказ, премия не рассчитывается
        if (result.getRiskClass() == null || !result.getRiskClass().isAcceptable()) {
            PackageType pt = company.getPackageType();
            result.setBaseRate(pt == null ? 0.0 : pt.getBaseRate());
            result.setKScore(Double.NaN);
            result.setKHistory(Double.NaN);
            result.setKService(Double.NaN);
            result.setKExposure(Double.NaN);
            result.setKAudit(Double.NaN);
            result.setAnnualPremium(Double.NaN);
            result.setEffectiveRate(Double.NaN);
            return;
        }

        PackageType packageType = company.getPackageType();
        double s = company.getInsuredSum();
        double t = packageType.getBaseRate();
        double kIndustry = result.getIndustryFactor();
        double kScore    = result.getRiskClass().getKScore();
        double kSize     = company.getSizeFactor();
        double kHistory  = company.getLossHistory().getKHistory();
        double kService  = calculateServiceFactor(company);
        double kExposure = calculateExposureFactor(company);
        double kAudit    = calculateAuditFactor(company);

        double premium = s * t * kIndustry * kScore * kSize * kHistory
                * kService * kExposure * kAudit;

        result.setBaseRate(t);
        result.setKScore(kScore);
        result.setKHistory(kHistory);
        result.setKService(kService);
        result.setKExposure(kExposure);
        result.setKAudit(kAudit);
        result.setSizeFactor(kSize);
        result.setAnnualPremium(premium);
        result.setEffectiveRate(s > 0 ? premium / s : 0.0);
    }

    /**
     * K_сервиса:
     *  — для пакета Лайт: 0.90 при подключённом комплаенс-аудите, иначе 1.00;
     *  — для остальных пакетов: 0.85 при непрерывном мониторинге, иначе 1.00.
     */
    private double calculateServiceFactor(Company company) {
        if (company.getPackageType() == PackageType.LIGHT) {
            return company.isComplianceAudit()
                    ? Coefficients.K_SERVICE_LIGHT_COMPLIANCE
                    : Coefficients.K_SERVICE_DEFAULT;
        }
        return company.isContinuousMonitoring()
                ? Coefficients.K_SERVICE_MONITORING_ON
                : Coefficients.K_SERVICE_DEFAULT;
    }

    private double calculateExposureFactor(Company company) {
        return switch (company.getExposure()) {
            case NONE  -> Coefficients.K_EXPOSURE_NONE;
            case OLD   -> Coefficients.K_EXPOSURE_OLD;
            case FRESH -> Coefficients.K_EXPOSURE_FRESH;
        };
    }

    private double calculateAuditFactor(Company company) {
        return company.isHasExternalAudit()
                ? Coefficients.K_AUDIT_PRESENT
                : Coefficients.K_AUDIT_ABSENT;
    }
}