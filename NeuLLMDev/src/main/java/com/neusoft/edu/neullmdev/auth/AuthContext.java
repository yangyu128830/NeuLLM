package com.neusoft.edu.neullmdev.auth;

import java.util.concurrent.Callable;

public final class AuthContext {
    private static final ThreadLocal<AuthUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        CURRENT.set(user);
    }

    public static AuthUser get() {
        return CURRENT.get();
    }

    public static AuthUser require() {
        AuthUser user = CURRENT.get();
        if (user == null) {
            throw new IllegalArgumentException("未登录或会话已过期");
        }
        return user;
    }

    public static void clear() {
        CURRENT.remove();
    }

    /** 在指定用户上下文中执行（用于 Reactor 切换线程后恢复登录态）。 */
    public static <T> T callWith(AuthUser user, Callable<T> action) {
        AuthUser previous = get();
        try {
            if (user != null) {
                set(user);
            }
            return action.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (previous != null) {
                set(previous);
            } else {
                clear();
            }
        }
    }

    public static void runWith(AuthUser user, Runnable action) {
        callWith(user, () -> {
            action.run();
            return null;
        });
    }
}
