package com.sanay3y.egy.data.model

import com.sanay3y.egy.R

enum class RequestStatus(val labelRes: Int) {
    PENDING(R.string.status_pending),
    QUOTED(R.string.status_quoted),
    ACCEPTED(R.string.status_accepted),
    IN_PROGRESS(R.string.status_in_progress),
    COMPLETED_BY_PROVIDER(R.string.status_completed_by_provider),
    COMPLETED_BY_CLIENT(R.string.status_completed_by_client)
}
