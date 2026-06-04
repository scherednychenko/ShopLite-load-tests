/**
 * guest_data.groovy
 * JSR223 PreProcessor (Groovy)
 *
 * Generates unique guest data per thread/iteration to avoid collisions.
 * Intended for "guest checkout" flows (no login, no payment).
 *
 * Variables produced:
 *  - email, firstName, lastName, phone
 *  - country, city, addressLine1, zip
 */

import java.time.Instant
import java.util.UUID

def threadNum = ctx.getThreadNum()            // 0-based
def iter = vars.getIteration()                // per-thread iteration counter
def ts = Instant.now().toEpochMilli()

// If CSV is provided (guest_profiles.csv), prefer it; otherwise generate.
def csvEmail = vars.get('email')
def csvFirst = vars.get('firstName')
def csvLast  = vars.get('lastName')

def useCsv = (csvEmail != null && csvEmail.trim().length() > 0)

if (!useCsv) {
    // Unique suffix (short but collision-resistant)
    def suffix = "${ts}_${threadNum}_${iter}_${UUID.randomUUID().toString().substring(0,8)}"

    vars.put('firstName', 'Perf')
    vars.put('lastName',  "Guest${threadNum}")
    vars.put('email',     "qa.perf+${suffix}@example.com")

    // E.164-ish placeholder (avoid using real numbers)
    // Keep stable length; include thread/iter to reduce duplicates
    def phoneBase = 700000000L + (threadNum * 1000L) + (iter % 1000)
    vars.put('phone', "+1${phoneBase}")

    vars.put('country', 'HR')
    vars.put('city', 'Zagreb')
    vars.put('addressLine1', "Perf Street ${threadNum}-${iter}")
    vars.put('zip', '10000')
} else {
    // If CSV is used, still make email unique to avoid collisions on repeated runs
    def suffix = "${ts}_${threadNum}_${iter}"
    def normalized = csvEmail.trim()

    // Insert +suffix before @ if possible
    def at = normalized.indexOf('@')
    if (at > 0) {
        def local = normalized.substring(0, at)
        def domain = normalized.substring(at)
        vars.put('email', "${local}+${suffix}${domain}")
    } else {
        vars.put('email', "${normalized}+${suffix}@example.com")
    }

    // Ensure required fields exist even if CSV row is incomplete
    if (csvFirst == null || csvFirst.trim().isEmpty()) vars.put('firstName', 'Perf')
    if (csvLast == null  || csvLast.trim().isEmpty())  vars.put('lastName',  "Guest${threadNum}")

    if (vars.get('phone') == null || vars.get('phone').trim().isEmpty()) {
        def phoneBase = 700000000L + (threadNum * 1000L) + (iter % 1000)
        vars.put('phone', "+1${phoneBase}")
    }

    if (vars.get('country') == null || vars.get('country').trim().isEmpty()) vars.put('country', 'HR')
    if (vars.get('city') == null    || vars.get('city').trim().isEmpty())    vars.put('city', 'Zagreb')
    if (vars.get('addressLine1') == null || vars.get('addressLine1').trim().isEmpty()) {
        vars.put('addressLine1', "Perf Street ${threadNum}-${iter}")
    }
    if (vars.get('zip') == null || vars.get('zip').trim().isEmpty()) vars.put('zip', '10000')
}

// Optional: log only once per thread to avoid log spam
def logged = vars.get('__guest_logged')
if (logged == null) {
    log.info("Guest data initialized: email=${vars.get('email')} thread=${threadNum}")
    vars.put('__guest_logged', 'true')
}
