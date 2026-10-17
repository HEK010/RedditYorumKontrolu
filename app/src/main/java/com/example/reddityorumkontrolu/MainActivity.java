package com.example.reddityorumkontrolu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String[] WORDS_TO_CHECK = {
            "amına koyayım", "amına koyim", "aminakoyim", "orospu çocuğu",
            "orospu cocugu", "orospu evladı", "orospuevladı", "bok gibi",
            "amcık", "amcik", "amına", "amina", "amk", "aq", "avrat",
            "boktan", "bok", "dalyarak", "daşak", "daşşak", "dasak", "dassak",
            "gavat", "göt", "got", "kahpe", "kaşar", "kaltak", "oç", "orospu",
            "öküz", "pezevenk", "piç", "pic", "puşt", "pust", "sikik", "sikim",
            "sikeyim", "sikerim", "siktir", "siktirgit", "sikiyim", "siktim",
            "sik", "yarrak", "yarak", "yavşak", "yavsak"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText commentInput = findViewById(R.id.commentInput);
        Button checkButton = findViewById(R.id.checkButton);
        TextView resultText = findViewById(R.id.resultText);

        checkButton.setOnClickListener(view -> {
            String comment = commentInput.getText().toString();
            if (comment.trim().isEmpty()) {
                commentInput.setError("Önce kontrol edilecek yorumu yaz veya yapıştır.");
                return;
            }

            Pattern pattern = buildWordPattern();
            String normalizedComment = normalizeForMatching(comment);
            Matcher matcher = pattern.matcher(normalizedComment);
            List<int[]> matchRanges = new ArrayList<>();
            while (matcher.find()) {
                matchRanges.add(new int[]{matcher.start(), matcher.end()});
            }

            StringBuilder maskedCommentBuilder = new StringBuilder(comment);
            for (int i = matchRanges.size() - 1; i >= 0; i--) {
                int[] range = matchRanges.get(i);
                maskedCommentBuilder.replace(range[0], range[1], "****");
            }
            String maskedComment = maskedCommentBuilder.toString();
            if (matchRanges.isEmpty()) {
                resultText.setText("Listedeki kelimelerden biri bulunmadı. Bu, yorumun kesinlikle temiz olduğu anlamına gelmez.\n\nYorum:\n" + comment);
            } else {
                resultText.setText("Küfür olabilecek " + matchRanges.size() + " ifade bulundu.\n\nSansürlenmiş görünüm:\n" + maskedComment);
            }
        });
    }

    private Pattern buildWordPattern() {
        LinkedHashSet<String> variants = new LinkedHashSet<>();
        for (String word : WORDS_TO_CHECK) {
            String normalizedWord = normalizeForMatching(word);
            variants.add(normalizedWord);

            if (!normalizedWord.contains(" ") && normalizedWord.length() >= 5) {
                for (int i = 0; i < normalizedWord.length(); i++) {
                    if (isVowel(normalizedWord.charAt(i))) {
                        variants.add(normalizedWord.substring(0, i) + normalizedWord.substring(i + 1));
                    }
                }
            }
        }

        String[] sortedWords = variants.toArray(new String[0]);
        Arrays.sort(sortedWords, (first, second) -> Integer.compare(second.length(), first.length()));

        StringBuilder alternatives = new StringBuilder();
        for (String word : sortedWords) {
            if (alternatives.length() > 0) {
                alternatives.append("|");
            }
            alternatives.append(Pattern.quote(word));
        }

        String regex = "(?u)(?<![\\p{L}\\p{N}_])[\\p{L}\\p{N}_]*(?:"
                + alternatives + ")[\\p{L}\\p{N}_]*(?![\\p{L}\\p{N}_])";
        return Pattern.compile(regex);
    }

    private String normalizeForMatching(String text) {
        StringBuilder normalized = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char character = Character.toLowerCase(text.charAt(i));
            switch (character) {
                case 'ı':
                    character = 'i';
                    break;
                case 'ç':
                    character = 'c';
                    break;
                case 'ğ':
                    character = 'g';
                    break;
                case 'ö':
                    character = 'o';
                    break;
                case 'ş':
                    character = 's';
                    break;
                case 'ü':
                    character = 'u';
                    break;
                case '0':
                    character = 'o';
                    break;
                case '1':
                    character = 'i';
                    break;
                case '3':
                    character = 'e';
                    break;
                case '4':
                case '@':
                    character = 'a';
                    break;
                case '$':
                    character = 's';
                    break;
                default:
                    break;
            }
            normalized.append(character);
        }
        return normalized.toString();
    }

    private boolean isVowel(char character) {
        return "aeiou".indexOf(character) >= 0;
    }
}