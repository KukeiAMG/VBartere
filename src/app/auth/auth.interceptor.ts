import {HttpHandlerFn, HttpInterceptor, HttpInterceptorFn, HttpRequest} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';
import {BehaviorSubject, catchError, filter, switchMap, tap, throwError} from 'rxjs';
import {jwtDecode} from "jwt-decode";

let isRefreshing$ = new BehaviorSubject<boolean>(false);

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const token = authService.token;

    if (!token) return next(req);

    // Если запрос уже идёт на обновление токена — пропускаем
    if (req.url.includes('refresh-token')) {
        return next(req);
    }

    function isTokenExpired(token: string): boolean {
        try {
            const decoded = jwtDecode(token);
            // Если есть поле `exp` (expiration time) и оно в прошлом — токен просрочен
            return decoded.exp ? decoded.exp * 1000 < Date.now() : false;
        } catch (e) {
            // Если токен невалидный (не JWT), считаем его просроченным
            return true;
        }
    }

    // Если токен не просрочен — просто добавляем его
    if (!isTokenExpired(token)) { // 🚀 Нужна функция проверки срока жизни
        return next(addToken(req, token));
    }

    // Иначе — обновляем
    return refreshAndProceed(authService, req, next);
};



const refreshAndProceed = (
  authService : AuthService,
  req : HttpRequest<any>,
  next : HttpHandlerFn
) => {
  if(!isRefreshing$.value){
    isRefreshing$.next(true);

    return authService.refreshAuthToken()
      .pipe(
        switchMap(res => {
          isRefreshing$.next(false);
          return next(addToken(req, res.accessToken)).pipe(
            tap(() =>{
              isRefreshing$.next(false);
            })
          )
        })
      )
  }

  if(req.url.includes('refresh')){return next(addToken(req, authService.token!))}

  return isRefreshing$.pipe(
    filter(isRefreshing => !isRefreshing),
    switchMap(res => {
      return next(addToken(req, authService.token!))
    })
  )

}

const addToken = (req : HttpRequest<any>, token : string) => {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  })
}

