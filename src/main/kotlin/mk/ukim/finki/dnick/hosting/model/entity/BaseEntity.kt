package mk.ukim.finki.dnick.hosting.model.entity

import org.hibernate.proxy.HibernateProxy

abstract class BaseEntity<T> {

    abstract fun extractId(): T

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val oEffectiveClass = if (o is HibernateProxy) o.hibernateLazyInitializer.persistentClass else o.javaClass
        val thisEffectiveClass =
            if ((this as Any) is HibernateProxy) (this as HibernateProxy).hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        val student = o as BaseEntity<*>
        return this.extractId() != null && (this.extractId() == student.extractId())
    }

    override fun hashCode(): Int {
        return if ((this as Any) is HibernateProxy) (this as HibernateProxy).hibernateLazyInitializer.persistentClass.hashCode()
        else javaClass.hashCode()
    }

    override fun toString(): String {
        return "${this::class.simpleName}@${this.extractId()?.toString() ?: ""}"
    }
}