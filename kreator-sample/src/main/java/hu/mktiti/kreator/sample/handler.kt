package hu.mktiti.kreator.sample

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject

@Injectable
class Handler(
        private val userRepo: UserRepo = inject()
) {

    fun listAll(): List<User> {
        return if (userRepo.authenticate("user", "pass")) {
            userRepo.listAll()
        } else {
            emptyList()
        }
    }

}
