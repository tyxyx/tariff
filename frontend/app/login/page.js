import { colors } from "@/styles/colors";
import Header from "@/components/ui/header";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { LoginForm } from "@/components/login-form";
import Link from "next/link";
import { Calculator } from "lucide-react";


 export default function LoginPage() {
   return (
     <div
       className="min-h-screen flex items-center justify-center p-4"
       style={{ backgroundColor: colors.background, color: colors.text }}
     >
       <div className="w-full max-w-md">
         {/* Header */}
         <div className="text-center mb-8">
           <Link
             href="/"
             className="inline-flex items-center gap-2 text-2xl font-bold text-foreground"
           >
             <Calculator className="h-8 w-8 text-primary" />
             TariffCalc Pro
           </Link>
           <p className="text-muted-foreground mt-2">Sign in to your account</p>
         </div>
         <Card>
              <CardHeader>
              </CardHeader>
              <CardContent>
                <LoginForm />
              </CardContent>
          </Card>
       </div>
     </div>
   );
 }