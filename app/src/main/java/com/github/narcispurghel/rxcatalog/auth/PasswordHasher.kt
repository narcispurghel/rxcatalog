package com.github.narcispurghel.rxcatalog.auth

import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordHasher
	@Inject
	constructor() {
		fun hash(password: String): String {
			val salt = ByteArray(SALT_LENGTH_BYTES).also(secureRandom::nextBytes)
			val hash = pbkdf2(password, salt, DEFAULT_ITERATIONS)
			return buildString {
				append(ALGORITHM)
				append(SEPARATOR)
				append(DEFAULT_ITERATIONS)
				append(SEPARATOR)
				append(base64Encoder.encodeToString(salt))
				append(SEPARATOR)
				append(base64Encoder.encodeToString(hash))
			}
		}

		fun verify(
			password: String,
			encodedHash: String,
		): Boolean {
			val parts = encodedHash.split(SEPARATOR)
			if (parts.size != 4 || parts[0] != ALGORITHM) {
				return false
			}

			val iterations = parts[1].toIntOrNull() ?: return false
			val salt = decodeBase64(parts[2]) ?: return false
			val expectedHash = decodeBase64(parts[3]) ?: return false
			val candidateHash = pbkdf2(password, salt, iterations)

			return candidateHash.contentEquals(expectedHash)
		}

		private fun pbkdf2(
			password: String,
			salt: ByteArray,
			iterations: Int,
		): ByteArray {
			val spec = PBEKeySpec(password.toCharArray(), salt, iterations, HASH_LENGTH_BITS)
			return try {
				secretKeyFactory.generateSecret(spec).encoded
			} finally {
				spec.clearPassword()
			}
		}

		private fun decodeBase64(value: String): ByteArray? =
			runCatching { base64Decoder.decode(value) }.getOrNull()

		private companion object {
			private const val ALGORITHM = "pbkdf2_sha256"
			private const val DEFAULT_ITERATIONS = 120_000
			private const val HASH_LENGTH_BITS = 256
			private const val SALT_LENGTH_BYTES = 16
			private const val SEPARATOR = "$"

			private val secureRandom = SecureRandom()
			private val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
			private val base64Encoder = Base64.getEncoder()
			private val base64Decoder = Base64.getDecoder()
		}
	}
