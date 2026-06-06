package ru.finuniv.retailshield.service;

import org.springframework.stereotype.Service;
import ru.finuniv.retailshield.model.PackageType;

/**
 * Анкета-помощник на лендинге: подбирает рекомендуемый тарифный пакет
 * по выбору галочек «что нужно страховать».
 *
 * Логика:
 *  — если выбран только регуляторный риск (юридическая защита) и компания
 *    малого/среднего размера — пакет Лайт;
 *  — если выбраны собственные убытки + ответственность перед третьими лицами,
 *    без репутационных и сервисов — пакет Базовый;
 *  — если выбраны репутационные расходы или 4 категории из 5 — Стандарт;
 *  — если выбраны дополнительные сервисы или все 5 категорий — Премиум.
 */
@Service
public class PackageRecommenderService {

    /** Параметры выбора клиента (соответствуют чек-боксам на экране 1). */
    public static class Request {
        public boolean coverOwnLosses;          // 1. Собственные убытки компании
        public boolean coverThirdParty;         // 2. Ответственность перед третьими лицами
        public boolean coverReputation;         // 3. Репутационные расходы и кризисы
        public boolean coverRegulatory;         // 4. Юридическая защита и регуляторика
        public boolean coverAdditionalServices; // 5. Дополнительные ИБ-сервисы
        public double annualRevenueRub;         // для отсечки по выручке для Лайта
    }

    public static class Recommendation {
        public final PackageType recommended;
        public final String rationale;

        public Recommendation(PackageType recommended, String rationale) {
            this.recommended = recommended;
            this.rationale = rationale;
        }
    }

    public Recommendation recommend(Request req) {
        int categoriesCount = countSelected(req);

        // Случай «ничего не выбрано» — Лайт как наиболее доступный вход
        if (categoriesCount == 0) {
            return new Recommendation(PackageType.LIGHT,
                    "Не выбраны категории покрытия — предлагаем начать с пакета Лайт "
                            + "как наиболее доступного варианта для знакомства с продуктом.");
        }

        // Лайт: только регуляторный риск + малый/средний бизнес
        if (req.coverRegulatory
                && !req.coverOwnLosses
                && !req.coverThirdParty
                && !req.coverReputation
                && !req.coverAdditionalServices
                && req.annualRevenueRub <= 1_000_000_000d) {
            return new Recommendation(PackageType.LIGHT,
                    "Выбран только регуляторный риск, выручка до 1 млрд ₽ — "
                            + "пакет Лайт даёт юридическую защиту при утечках ПДн без "
                            + "переплаты за компоненты бизнес-простоя.");
        }

        // Премиум: все 5 категорий или явно отмечены доп. сервисы
        if (categoriesCount == 5 || req.coverAdditionalServices) {
            return new Recommendation(PackageType.PREMIUM,
                    "Выбраны дополнительные ИБ-сервисы или полный набор категорий — "
                            + "пакет Премиум даёт максимальные сублимиты, параметрический "
                            + "аванс при инциденте и непрерывный мониторинг периметра.");
        }

        // Стандарт: репутационные или 3-4 категории
        if (req.coverReputation || categoriesCount >= 3) {
            return new Recommendation(PackageType.STANDARD,
                    "Выбраны репутационные расходы или 3+ категории покрытия — "
                            + "пакет Стандарт включает полное покрытие собственных убытков, "
                            + "ответственности, репутации и базовый сервисный пакет.");
        }

        // Базовый: собственные убытки + ответственность, без репутации
        return new Recommendation(PackageType.BASIC,
                "Выбраны собственные убытки и/или ответственность перед третьими "
                        + "лицами без репутационных расходов и сервисов — пакет Базовый "
                        + "предоставляет необходимый минимум по доступной цене.");
    }

    private int countSelected(Request req) {
        int n = 0;
        if (req.coverOwnLosses) n++;
        if (req.coverThirdParty) n++;
        if (req.coverReputation) n++;
        if (req.coverRegulatory) n++;
        if (req.coverAdditionalServices) n++;
        return n;
    }
}