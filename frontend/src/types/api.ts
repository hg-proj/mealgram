// 실패 응답 형식

export type FieldError = { field: string; message: string };

export type ApiErrorBody = {
  success: false;
  error: { code: string; message: string; fieldErrors?: FieldError[] };
};
