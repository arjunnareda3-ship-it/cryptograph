package com.example.data.model

data class CyberProjectIdea(
    val id: String,
    val title: String,
    val hindiSummary: String,
    val category: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val vivaScopeRating: String, // 5/5, 4.5/5
    val abstractText: String,
    val problemStatement: String,
    val modules: List<String>,
    val techStack: List<String>,
    val vivaQuestions: List<VivaQuestion>,
    val synopsisTemplate: String
)

data class VivaQuestion(
    val question: String,
    val answer: String,
    val tips: String
)

data class CyberQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val category: String
)

object ProjectGuideData {

    val projectIdeas = listOf(
        CyberProjectIdea(
            id = "proj_cybershield",
            title = "CyberShield: Mobile Cryptography Lab & App Security Auditor",
            hindiSummary = "मोबाइल क्रिप्टोग्राफी लैब, ऐप प्राइवेसी एनालाइजर और थ्रेट स्कैनर (यह प्रोजेक्ट आप सीधे सबमिट और डेमो कर सकते हैं!)",
            category = "Cryptographic Systems & Mobile Security",
            difficulty = "Intermediate to Advanced",
            vivaScopeRating = "5.0 / 5.0 (Top Examiner Choice)",
            abstractText = "CyberShield is an all-in-one Android cybersecurity suite built with Kotlin and Jetpack Compose. It bridges theoretical cryptographic concepts (AES-256 GCM, RSA-2048, Hashing, Zero-width steganography) with practical mobile forensics, real-time phishing URL heuristic analysis, device port scanning, and installed app privacy risk auditing.",
            problemStatement = "Most college students understand security theories on paper but lack practical toolkits to inspect packet endpoints, verify cipher hashes, or audit permissions that malicious spy apps exploit. CyberShield provides a tangible, verifiable demonstration of modern defense mechanisms on mobile endpoints.",
            modules = listOf(
                "Symmetric Cryptography Module (AES-256 GCM/CBC with Keystore IV)",
                "Asymmetric Cryptography Module (RSA-2048 keypair generation & encryption)",
                "Classical Cryptanalysis (Caesar brute-force cracker & Vigenère cipher)",
                "Steganography Engine (Zero-width character invisible payload injection)",
                "URL Phishing Heuristics Scanner (Homograph, Punycode, TLDs, Entropy)",
                "Network & Socket Port Inspector (TCP handshakes on ports 21, 22, 80, 443)",
                "App Permission Threat Auditor (Dangerous permission scoring & risk levels)"
            ),
            techStack = listOf(
                "Kotlin 2.x & Jetpack Compose (Modern Declarative UI)",
                "Android Keystore & javax.crypto (AES, RSA, SHA-256)",
                "Room Database & Coroutines Flow (Local Persistence & Asynchronous scans)",
                "Java Socket API & Android ConnectivityManager"
            ),
            vivaQuestions = listOf(
                VivaQuestion(
                    question = "Why is AES-GCM preferred over AES-CBC?",
                    answer = "AES-GCM (Galois/Counter Mode) provides Authenticated Encryption with Associated Data (AEAD). It guarantees both confidentiality and ciphertext integrity via an authentication tag, preventing bit-flipping attacks common in CBC mode.",
                    tips = "Mention IV uniqueness: never reuse an IV with the same key in GCM mode."
                ),
                VivaQuestion(
                    question = "What is the difference between Hashing and Encryption?",
                    answer = "Encryption is a two-way mathematical function requiring a secret key to decrypt back to plaintext. Hashing is a one-way deterministic algorithm (pre-image resistance) used for integrity verification and password storage.",
                    tips = "Emphasize that you cannot 'decrypt' a hash; you can only search collisions or rainbow tables."
                ),
                VivaQuestion(
                    question = "How does zero-width steganography conceal data?",
                    answer = "It encodes ASCII/UTF-8 bytes into binary bits, replacing '0' with Zero-Width Space (U+200B) and '1' with Zero-Width Non-Joiner (U+200C). To human eyes and standard text viewers, characters take zero width and appear completely invisible.",
                    tips = "Explain practical covert channel transmission in chat messages and emails."
                )
            ),
            synopsisTemplate = """
                PROJECT SYNOPSIS: CyberShield - Mobile Cybersecurity & Cryptography Lab
                
                1. INTRODUCTION:
                In today's interconnected digital ecosystem, mobile devices are prime targets for eavesdropping, phishing, and invasive spyware. CyberShield is developed as a comprehensive security auditing suite for Android.
                
                2. OBJECTIVES:
                - Implement cryptographic primitives (AES-256, RSA-2048, SHA-256).
                - Provide visual brute-force cryptanalysis for classical ciphers.
                - Demonstrate zero-width steganography for secure covert channels.
                - Inspect network ports and detect malicious phishing URLs using heuristics.
                - Audit installed Android applications for overprivileged permissions.
                
                3. SYSTEM ARCHITECTURE:
                MVVM Architecture with Jetpack Compose UI, Room persistence, and Cryptography Service layer.
                
                4. HARDWARE & SOFTWARE SPECIFICATIONS:
                - OS: Android 8.0 (API 26) or higher
                - Programming Language: Kotlin
                - Persistence: SQLite via Android Jetpack Room
                - Cryptographic Providers: Android Keystore, javax.crypto, java.security
            """.trimIndent()
        ),
        CyberProjectIdea(
            id = "proj_stego_vault",
            title = "PixelCrypt: Steganography & Encrypted Media Vault",
            hindiSummary = "इमेज में सीक्रेट डेटा छुपाने (LSB Steganography) और AES-256 एन्क्रिप्शन वाला सेफ वॉल्ट",
            category = "Steganography & Digital Forensics",
            difficulty = "Intermediate",
            vivaScopeRating = "4.8 / 5.0",
            abstractText = "PixelCrypt allows users to hide confidential passwords, private keys, and textual secrets inside image pixels using Least Significant Bit (LSB) steganography, combined with AES-256 pre-encryption.",
            problemStatement = "Encrypted files attract adversary attention because their contents look scrambled (high Shannon entropy). Steganography conceals the very existence of the communication, hiding secret messages in innocuous cover carriers.",
            modules = listOf(
                "LSB Bitmap Encoder & Decoder",
                "Pre-Encryption Layer (AES-GCM before embedding)",
                "Steganalysis & Histogram Chi-Square Detector",
                "Secure PIN Protected Private Vault"
            ),
            techStack = listOf("Kotlin", "Android Graphics Bitmap APIs", "AES-256", "Room DB"),
            vivaQuestions = listOf(
                VivaQuestion(
                    question = "What is LSB (Least Significant Bit) Steganography?",
                    answer = "LSB alters the least significant bit of color channel bytes (RGB). Changing 0 to 1 modifies the pixel color by 1 unit out of 256, which is imperceptible to the human eye.",
                    tips = "Mention why PNG (lossless) is required instead of JPEG (lossy compression destroys LSBs)."
                ),
                VivaQuestion(
                    question = "What is Steganalysis?",
                    answer = "The forensic science of discovering and decoding covert messages hidden in media files through statistical analysis (e.g. sample pair analysis, chi-squared testing).",
                    tips = "Explain how encrypting data before LSB embedding makes statistical detection harder."
                )
            ),
            synopsisTemplate = "PixelCrypt Project Synopsis: Implementation of LSB Steganography and AES-256 encryption on Android."
        ),
        CyberProjectIdea(
            id = "proj_phishing_dns",
            title = "PhishGuard: AI & Heuristic Phishing Detector with DNS Guard",
            hindiSummary = "फिशिंग लिंक और फ्रॉड वेबसाइट्स को पहचानने वाला रियल-टाइम URL और DNS स्कैनर",
            category = "Web & Network Security",
            difficulty = "Intermediate",
            vivaScopeRating = "4.9 / 5.0",
            abstractText = "PhishGuard detects fraudulent websites and credential harvesting links using 12+ multi-factor heuristic checks, domain entropy calculation, homograph lookalike detection, and suspicious TLD classification.",
            problemStatement = "Phishing is responsible for over 80% of reported cyber incidents. Users click masked links without checking character substitutions (like Cyrillic 'a' vs Latin 'a').",
            modules = listOf(
                "Homograph & IDN Punycode Analyzer",
                "Domain Entropy & DGA Detection",
                "Keyword & Subdomain Depth Inspection",
                "SSL/TLS Certificate Validity & Redirection Tracker"
            ),
            techStack = listOf("Kotlin", "Java URI & Regex", "OkHttp", "Jetpack Compose"),
            vivaQuestions = listOf(
                VivaQuestion(
                    question = "What is an IDN Homograph Attack?",
                    answer = "An attack where an attacker registers a domain name visually identical to a legitimate brand using lookalike characters from international alphabets (e.g., Cyrillic 'а' U+0430 instead of Latin 'a' U+0061).",
                    tips = "Punycode (xn--) was introduced to represent international domains in ASCII."
                )
            ),
            synopsisTemplate = "PhishGuard Project Synopsis: Real-time Phishing Detection using Heuristics and Shannon Entropy."
        ),
        CyberProjectIdea(
            id = "proj_app_audit",
            title = "AppPrivacy Radar: Android Spyware & Permission Risk Scanner",
            hindiSummary = "मोबाइल ऐप्स की जासूसी परमीशन्स (कैमरा, माइक, SMS, लोकेशन) को स्कैन कर प्राइवेसी स्कोर देने वाला टूल",
            category = "Endpoint Security & Privacy",
            difficulty = "Beginner to Intermediate",
            vivaScopeRating = "4.7 / 5.0",
            abstractText = "Scans all installed applications on the mobile device, examines declared and dangerous permissions (SMS, Camera, Microphone, Background Location, Call Logs), and calculates a cumulative Privacy Threat Score.",
            problemStatement = "Many casual apps and games quietly request invasive runtime permissions to harvest contact books and track user locations for advertising or predatory loan scamming.",
            modules = listOf(
                "PackageManager Manifest Permission Extractor",
                "Dynamic Risk Matrix & Scoring Algorithm",
                "Background Service & Tracker Detector",
                "Hardening Guide & Permission Revocation Shortcuts"
            ),
            techStack = listOf("Kotlin", "Android PackageManager", "Jetpack Compose", "M3 UI"),
            vivaQuestions = listOf(
                VivaQuestion(
                    question = "What is the difference between Normal and Dangerous permissions in Android?",
                    answer = "Normal permissions (like INTERNET, VIBRATE) pose little risk to privacy and are granted automatically at install time. Dangerous permissions (like READ_SMS, CAMERA, ACCESS_FINE_LOCATION) access sensitive private data and require explicit runtime user consent.",
                    tips = "Android 6.0 (Marshmallow, API 23) introduced runtime permissions."
                )
            ),
            synopsisTemplate = "AppPrivacy Radar Project Synopsis: Dynamic Android Application Permission Analysis and Privacy Hardening."
        ),
        CyberProjectIdea(
            id = "proj_wifi_ids",
            title = "NetHawk: Wireless Network Analyzer & Rogue AP Detector",
            hindiSummary = "वाई-फाई नेटवर्क की सुरक्षा, रोग एक्सेस पॉइंट (Evil Twin) और ओपन पोर्ट्स को चेक करने वाला टूल",
            category = "Wireless & Network Defense",
            difficulty = "Advanced",
            vivaScopeRating = "4.9 / 5.0",
            abstractText = "NetHawk diagnoses local Wi-Fi networks for weak encryption (WEP, unencrypted Open Wi-Fi, WPS vulnerabilities), detects potential Evil Twin rogue access points, and scans network devices for exposed administrative ports.",
            problemStatement = "Public Wi-Fi networks in cafes and airports are susceptible to Evil Twin attacks and ARP poisoning where threat actors snoop unencrypted sessions.",
            modules = listOf(
                "Wi-Fi Security Type Evaluator (WPA2 vs WPA3 vs Open)",
                "Local Subnet Host Discovery (ARP / Ping sweep)",
                "Socket Port Scanner (Common ports 21, 22, 23, 80, 443)",
                "Evil Twin / Duplicate SSID Alert Engine"
            ),
            techStack = listOf("Kotlin", "Android WifiManager", "Java Socket API", "Coroutines"),
            vivaQuestions = listOf(
                VivaQuestion(
                    question = "What is an Evil Twin Attack?",
                    answer = "A fraudulent Wi-Fi access point that mimics the SSID of a legitimate legitimate network (e.g. 'Airport-Free-WiFi'). When victims connect, the attacker performs Man-in-the-Middle traffic interception.",
                    tips = "Mitigation includes VPN tunnels, WPA3 enterprise authentication, and HTTPS Strict Transport Security (HSTS)."
                )
            ),
            synopsisTemplate = "NetHawk Project Synopsis: Wireless Network Security Assessment and Port Scanning Tool."
        ),
        CyberProjectIdea(
            id = "proj_zero_pass",
            title = "KryptonPass: Zero-Knowledge Password Vault & Entropy Shield",
            hindiSummary = "जीरो-नॉलेज आर्किटेक्चर वाला सुरक्षित पासवर्ड मैनेजर, जिसमें मास्टर की कभी स्टोर नहीं होती",
            category = "Applied Cryptography & Authentication",
            difficulty = "Intermediate to Advanced",
            vivaScopeRating = "5.0 / 5.0",
            abstractText = "KryptonPass is an offline, zero-knowledge password vault. Master passwords are never stored anywhere; instead, PBKDF2 with 100,000 iterations and SHA-256 derives a 256-bit encryption key on-the-fly to unlock the encrypted Room SQLite database.",
            problemStatement = "Centralized cloud password vaults risk catastrophic data breaches. An offline, client-side zero-knowledge vault provides complete user sovereignty over digital credentials.",
            modules = listOf(
                "PBKDF2 Key Derivation Function (KDF)",
                "AES-256 GCM Database Vault Encryption",
                "Password Shannon Entropy & GPU Crack Estimator",
                "Biometric Unlock (Fingerprint / Face via AndroidX Biometric)"
            ),
            techStack = listOf("Kotlin", "PBKDF2WithHmacSHA256", "AES-GCM", "Room DB"),
            vivaQuestions = listOf(
                VivaQuestion(
                    question = "What is Zero-Knowledge Architecture?",
                    answer = "A security design where the system has no knowledge of, or access to, the user's master encryption key or plaintext data. Even if the database is stolen, it cannot be decrypted without the user's unrecorded master passphrase.",
                    tips = "Key point: PBKDF2 adds a salt and high iteration count to thwart rainbow tables and GPU brute-forcing."
                )
            ),
            synopsisTemplate = "KryptonPass Project Synopsis: Zero-Knowledge Password Storage utilizing PBKDF2 and AES-256."
        )
    )

    val vivaCheatSheet = listOf(
        VivaQuestion(
            question = "What is the CIA Triad in Cyber Security?",
            answer = "Confidentiality (protecting data from unauthorized disclosure, achieved via Encryption), Integrity (ensuring data is not tampered with, achieved via Hashing/Digital Signatures), and Availability (ensuring systems remain accessible to authorized users, defended against DoS/DDoS).",
            tips = "Fundamental question! Always start with this definition."
        ),
        VivaQuestion(
            question = "What is the difference between Symmetric and Asymmetric Encryption?",
            answer = "Symmetric encryption uses the SAME single secret key for both encryption and decryption (e.g. AES, DES). It is fast and efficient for bulk data. Asymmetric encryption uses a mathematically linked KEY PAIR: a Public Key (for encryption) and a Private Key (for decryption) (e.g. RSA, ECC). It solves the key-distribution problem.",
            tips = "Mention that real-world protocols like TLS/HTTPS use Hybrid Encryption (RSA/ECC for key exchange, AES for session data)."
        ),
        VivaQuestion(
            question = "What is a Salt in cryptography and why is it used?",
            answer = "A salt is random, unique data appended to plaintext passwords before hashing. It prevents Rainbow Table attacks and ensures two identical passwords generate completely distinct hashes.",
            tips = "Salts do not need to be secret, but they must be unique per password."
        ),
        VivaQuestion(
            question = "Explain SQL Injection (SQLi) and its prevention.",
            answer = "SQLi occurs when untrusted user input is directly concatenated into a dynamic database query, allowing attackers to execute unauthorized SQL statements. Prevention: Use Prepared Statements (Parameterized Queries), ORM frameworks like Room/Hibernate, and input validation.",
            tips = "Give the classic example: ' OR '1'='1."
        ),
        VivaQuestion(
            question = "What is Cross-Site Scripting (XSS)?",
            answer = "XSS is a vulnerability where malicious JavaScript scripts are injected into trusted web applications and executed in the victim's browser, leading to cookie/session theft. Types: Stored XSS, Reflected XSS, and DOM-based XSS. Mitigation: Context-aware HTML entity encoding and Content Security Policy (CSP).",
            tips = "Distinguish between client-side XSS and server-side SQLi."
        ),
        VivaQuestion(
            question = "What is the Indian Information Technology Act, 2000 (IT Act 2000)?",
            answer = "The primary law in India dealing with cybercrime and electronic commerce. Key sections: Section 43 (penalty for unauthorized access/data copying), Section 66 (computer related offenses/hacking), Section 66C (identity theft), Section 66D (cheating by personation using computer resource), Section 66E (privacy violation), Section 69 (monitoring/decryption directives).",
            tips = "External BCA examiners in India almost ALWAYS ask 1-2 questions on the IT Act 2000."
        ),
        VivaQuestion(
            question = "What is a Man-in-the-Middle (MitM) Attack?",
            answer = "An attack where the adversary secretly relays and possibly alters the communication between two parties who believe they are directly communicating with each other. Mitigation: Mutual TLS (mTLS), SSL Certificate Pinning, and end-to-end encryption.",
            tips = "Mention ARP spoofing and DNS spoofing as LAN-based MitM methods."
        ),
        VivaQuestion(
            question = "What is Digital Signature and how does it work?",
            answer = "A digital signature guarantees authentication, non-repudiation, and integrity. The sender generates a cryptographic hash of the document and encrypts the hash with their PRIVATE key. The receiver decrypts the signature with the sender's PUBLIC key and verifies the hash matches.",
            tips = "Remember: Sender signs with Private Key; Receiver verifies with Public Key."
        )
    )

    val quizQuestions = listOf(
        CyberQuizQuestion(
            id = 1,
            question = "Which encryption standard uses 128, 192, or 256-bit symmetric keys?",
            options = listOf("RSA", "AES (Advanced Encryption Standard)", "Diffie-Hellman", "MD5"),
            correctIndex = 1,
            explanation = "AES is the worldwide symmetric block cipher standard approved by NIST, supporting 128, 192, and 256-bit key sizes.",
            category = "Cryptography"
        ),
        CyberQuizQuestion(
            id = 2,
            question = "Which principle ensures that data has not been altered or tampered with in transit?",
            options = listOf("Confidentiality", "Non-repudiation", "Integrity", "Availability"),
            correctIndex = 2,
            explanation = "Integrity guarantees that unauthorized modifications (additions, deletions, tampering) have not occurred.",
            category = "Security Fundamentals"
        ),
        CyberQuizQuestion(
            id = 3,
            question = "In asymmetric cryptography (e.g. RSA), which key is used to DECRYPT a message encrypted by the sender?",
            options = listOf("Sender's Public Key", "Sender's Private Key", "Recipient's Public Key", "Recipient's Private Key"),
            correctIndex = 3,
            explanation = "The sender encrypts the plaintext with the recipient's public key; only the recipient's private key can decrypt it.",
            category = "Cryptography"
        ),
        CyberQuizQuestion(
            id = 4,
            question = "Which section of the Indian IT Act 2000 specifically penalizes identity theft using computer resources?",
            options = listOf("Section 43", "Section 66C", "Section 67", "Section 72"),
            correctIndex = 1,
            explanation = "Section 66C of the IT Act 2000 prescribes imprisonment up to 3 years and fine for fraudulent use of another person's electronic signature, password, or unique identification feature.",
            category = "Cyber Law"
        ),
        CyberQuizQuestion(
            id = 5,
            question = "What is the primary vulnerability prevented by using Prepared Statements with parameterized queries?",
            options = listOf("Cross-Site Scripting (XSS)", "SQL Injection (SQLi)", "Buffer Overflow", "DDoS"),
            correctIndex = 1,
            explanation = "Prepared statements ensure user input is treated strictly as data literals rather than executable SQL code fragments.",
            category = "Application Security"
        ),
        CyberQuizQuestion(
            id = 6,
            question = "Which port is standard for HTTPS (HTTP over TLS/SSL)?",
            options = listOf("Port 21", "Port 80", "Port 443", "Port 8080"),
            correctIndex = 2,
            explanation = "Port 443 is the standard TCP port for encrypted HTTPS traffic.",
            category = "Network Security"
        ),
        CyberQuizQuestion(
            id = 7,
            question = "What does LSB stand for in digital image steganography?",
            options = listOf("Large Scale Bit", "Least Significant Bit", "Linear Synchronous Byte", "Lossless Storage Block"),
            correctIndex = 1,
            explanation = "Least Significant Bit refers to the lowest bit (bit 0) of a color component byte, modifying which produces zero perceptible color difference.",
            category = "Steganography"
        ),
        CyberQuizQuestion(
            id = 8,
            question = "Why is MD5 no longer recommended for cryptographic signatures and password storage?",
            options = listOf("It requires too much RAM", "It is only 64 bits", "Practical collision attacks exist", "It cannot hash text"),
            correctIndex = 2,
            explanation = "Researchers demonstrated practical collision attacks where two different inputs produce the exact same MD5 digest.",
            category = "Cryptography"
        )
    )
}
