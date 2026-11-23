package com.raqeeb.app.data

import com.raqeeb.app.domain.model.IslamicReminder

object TrustedAzkarLibrary {
    val allReminders = listOf(
        IslamicReminder("﴿ أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ ﴾", "\"Does he not know that Allah sees?\"", "سورة العلق - آية 14", "Surah Al-Alaq 14"),
        IslamicReminder("﴿ قُل لِّلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ وَيَحْفَظُوا فُرُوجَهُمْ ۚ ذَٰلِكَ أَزْكَىٰ لَهُمْ ﴾", "\"Tell the believing men to lower their gaze and guard their modesty.\"", "سورة النور - آية 30", "Surah An-Nur 30"),
        IslamicReminder("﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾", "\"Unquestionably, by the remembrance of Allah hearts are assured.\"", "سورة الرعد - آية 28", "Surah Ar-Ra'd 28"),
        IslamicReminder("﴿ وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ﴾", "\"And whoever fears Allah - He will make for him a way out.\"", "سورة الطلاق - آية 2", "Surah At-Talaq 2"),
        IslamicReminder("«مَن تَرَكَ شيئًا للهِ عوَّضَه اللهُ خيرًا منه»", "\"Whoever leaves something for Allah, Allah will replace it with better.\"", "حديث صحيح - مسند أحمد", "Sahih - Musnad Ahmad"),
        IslamicReminder("«يا مقلِّبَ القلوبِ ثبِّت قلبي على دينِكَ»", "\"O Turner of hearts, keep my heart firm upon Your religion.\"", "حديث صحيح - الترمذي", "Sahih - At-Tirmidhi"),
        IslamicReminder("«اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى»", "\"O Allah, I ask You for guidance, piety, chastity and self-sufficiency.\"", "صحيح مسلم", "Sahih Muslim"),
        IslamicReminder("«أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ»", "\"We have reached the morning and at this very time all sovereignty belongs to Allah.\"", "أذكار الصباح - حصن المسلم", "Morning Adhkar - Hisn al-Muslim"),
        IslamicReminder("«أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ»", "\"We have reached the evening and at this very time all sovereignty belongs to Allah.\"", "أذكار المساء - حصن المسلم", "Evening Adhkar - Hisn al-Muslim"),
        IslamicReminder("«بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ» (ثلاثاً)", "\"In the name of Allah with whose name nothing is harmed on earth nor in heaven.\"", "أذكار الصباح والمساء - أبو داود والترمذي", "Hisn al-Muslim - Sahih"),
        IslamicReminder("«رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا» (ثلاثاً)", "\"I am pleased with Allah as a Lord, and Islam as a religion, and Muhammad as a Prophet.\"", "أذكار الصباح والمساء - حصن المسلم", "Hisn al-Muslim"),
        IslamicReminder("«حَسْبِيَ اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ» (سبع مرات)", "\"Allah is sufficient for me. There is none worthy of worship but Him.\"", "أذكار الصباح والمساء - حصن المسلم", "Hisn al-Muslim"),
        IslamicReminder("«اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ»", "\"O Allah, help me to remember You, to give thanks to You, and to worship You in the best of manners.\"", "سنن أبي داود - حديث صحيح", "Sunan Abi Dawud - Sahih"),
        IslamicReminder("«لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ»", "\"There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.\"", "دعاء الكرب - سورة الأنبياء 87", "Surah Al-Anbiya 87"),
        IslamicReminder("«سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ»", "\"Glory is to Allah and all praise is to Him, Glory is to Allah the Supreme.\"", "متفق عليه - البخاري ومسلم", "Sahih al-Bukhari & Muslim")
    )
}
