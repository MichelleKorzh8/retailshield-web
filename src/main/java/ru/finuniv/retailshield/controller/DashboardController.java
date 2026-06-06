package ru.finuniv.retailshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.finuniv.retailshield.data.RiskMapLoader;
import ru.finuniv.retailshield.model.RiskMap;
import ru.finuniv.retailshield.model.Threat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Дашборд карты отраслевых рисков (Блок 2). Показывает:
 *  — матрицу «частота × ущерб» с цветовой кодировкой;
 *  — круговую диаграмму распределения ALE между угрозами;
 *  — столбчатую диаграмму нагрузки на группы факторов;
 *  — таблицу угроз с описанием и рекомендациями по покрытию.
 *
 * Все диаграммы рисуются на чистом SVG без внешних библиотек.
 */
@Controller
public class DashboardController {

    private final RiskMapLoader riskMapLoader;

    @Autowired
    public DashboardController(RiskMapLoader riskMapLoader) {
        this.riskMapLoader = riskMapLoader;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        RiskMap map = riskMapLoader.getMap();

        // 1. Матрица частота × ущерб
        model.addAttribute("matrix", buildMatrix(map));

        // 2. Pie chart данных
        PieData pie = buildPie(map);
        model.addAttribute("pie", pie);

        // 3. Столбцы по группам факторов
        BarData bars = buildBars(map);
        model.addAttribute("bars", bars);

        model.addAttribute("riskMap", map);
        model.addAttribute("pageTitle", "Карта отраслевых рисков");
        return "dashboard";
    }

    // === МАТРИЦА ===

    public static class MatrixCell {
        public String color;
        public List<Threat> threats = new ArrayList<>();
    }

    public static class MatrixData {
        public String[] frequencyLevels = {"Очень высокая", "Высокая", "Средняя", "Низкая"};
        public String[] damageLevels = {"До 30 млн", "30-100 млн", "100-500 млн"};
        public Map<String, MatrixCell> cells = new LinkedHashMap<>();
    }

    private MatrixData buildMatrix(RiskMap map) {
        MatrixData m = new MatrixData();
        Map<String, MatrixCell> map2 = new HashMap<>();

        // Инициализируем все ячейки
        for (String f : m.frequencyLevels) {
            for (String d : m.damageLevels) {
                String key = f + "|" + d;
                MatrixCell cell = new MatrixCell();
                cell.color = cellColor(f, d);
                map2.put(key, cell);
            }
        }

        // Распределяем угрозы по ячейкам
        for (Threat t : map.getThreats()) {
            String bucket = damageBucket(t.getLossMax());
            String key = t.getFrequencyCategory() + "|" + bucket;
            MatrixCell cell = map2.get(key);
            if (cell != null) cell.threats.add(t);
        }
        m.cells = new LinkedHashMap<>(map2);
        return m;
    }

    private String damageBucket(double lossMax) {
        if (lossMax <= 30_000_000d) return "До 30 млн";
        if (lossMax <= 100_000_000d) return "30-100 млн";
        return "100-500 млн";
    }

    private String cellColor(String freq, String damage) {
        int f = switch (freq) {
            case "Очень высокая" -> 4;
            case "Высокая"       -> 3;
            case "Средняя"       -> 2;
            default              -> 1;
        };
        int d = switch (damage) {
            case "100-500 млн" -> 3;
            case "30-100 млн"  -> 2;
            default            -> 1;
        };
        int total = f + d;
        return switch (total) {
            case 2 -> "#f0f0ee";
            case 3 -> "#dddbd7";
            case 4 -> "#bfbcb6";
            case 5 -> "#9a9690";
            case 6 -> "#74706a";
            default -> "#4a4744";
        };
    }

    // === PIE (ALE между угрозами) ===

    public static class PieData {
        public List<PieSlice> slices = new ArrayList<>();
        public double total;
    }

    public static class PieSlice {
        public String code;
        public String name;
        public double ale;
        public double sharePercent;
        public String pathD;   // SVG path для сектора
        public String color;
    }

    private static final String[] PALETTE = {
            "#4a4744","#6a6660","#8a8680","#a8a4a0",
            "#b8b4b0","#c8c4c0","#d8d4d0","#e0dcd8"
    };

    private PieData buildPie(RiskMap map) {
        PieData pie = new PieData();

        // Считаем «эталонный» ALE без скоринга клиента: TEF × средний_ущерб.
        // Это сравнительная картина по отрасли.
        double total = 0.0;
        for (Threat t : map.getThreats()) {
            total += t.getTef() * t.getLossAverage();
        }
        pie.total = total;

        int cx = 200, cy = 200, r = 150;
        double startAngle = -Math.PI / 2;
        int idx = 0;

        for (Threat t : map.getThreats()) {
            double ale = t.getTef() * t.getLossAverage();
            double share = total > 0 ? ale / total : 0;
            double angle = share * 2 * Math.PI;
            double endAngle = startAngle + angle;
            double x1 = cx + r * Math.cos(startAngle);
            double y1 = cy + r * Math.sin(startAngle);
            double x2 = cx + r * Math.cos(endAngle);
            double y2 = cy + r * Math.sin(endAngle);
            int largeArc = angle > Math.PI ? 1 : 0;

            PieSlice slice = new PieSlice();
            slice.code = t.getCode();
            slice.name = t.getName();
            slice.ale = ale;
            slice.sharePercent = share * 100;
            slice.color = PALETTE[idx % PALETTE.length];
            slice.pathD = String.format(java.util.Locale.US,
                    "M%d,%d L%.2f,%.2f A%d,%d 0 %d,1 %.2f,%.2f Z",
                    cx, cy, x1, y1, r, r, largeArc, x2, y2);
            pie.slices.add(slice);

            startAngle = endAngle;
            idx++;
        }
        return pie;
    }

    // === BARS (нагрузка по группам факторов) ===

    public static class BarData {
        public List<BarItem> items = new ArrayList<>();
        public int maxCount;
    }

    public static class BarItem {
        public String code;
        public String label;
        public int count;
        public int barHeightPx; // высота прямоугольника в пикселях
        public int yPx;         // координата y для прямоугольника
    }

    private BarData buildBars(RiskMap map) {
        String[] groups = {"А", "Б", "В", "Г", "Д"};
        String[] groupNames = {
                "А. Регуляторика",
                "Б. Техническая защита",
                "В. Персонал",
                "Г. Резервирование",
                "Д. Подрядчики"
        };

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String g : groups) counts.put(g, 0);
        for (Threat t : map.getThreats()) {
            if (t.getFactorGroups() == null) continue;
            for (String fg : t.getFactorGroups()) {
                counts.merge(fg, 1, Integer::sum);
            }
        }

        int max = counts.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int chartHeight = 240;
        int baseY = chartHeight - 40;

        BarData bd = new BarData();
        bd.maxCount = max;
        for (int i = 0; i < groups.length; i++) {
            int count = counts.get(groups[i]);
            int barH = (int) ((double) count / max * (baseY - 30));
            BarItem b = new BarItem();
            b.code = groups[i];
            b.label = groupNames[i];
            b.count = count;
            b.barHeightPx = barH;
            b.yPx = baseY - barH;
            bd.items.add(b);
        }
        return bd;
    }
}